package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.EntityNotFoundException;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidDataException;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
public class MetadataAggregatorServiceImpl implements MetadataAggregatorService {
    private final TaxonomyEntityService taxonomyEntityService;
    private final CompetenceAimService competenceAimService;
    private final CustomFieldService customFieldService;
    private final PublicIdValidator publicIdValidator;

    public MetadataAggregatorServiceImpl(TaxonomyEntityService taxonomyEntityService,
            CompetenceAimService competenceAimService, CustomFieldService customFieldService,
            PublicIdValidator publicIdValidator) {
        this.taxonomyEntityService = taxonomyEntityService;
        this.competenceAimService = competenceAimService;
        this.customFieldService = customFieldService;
        this.publicIdValidator = publicIdValidator;
    }

    private MetadataDto createEmptyDto(String publicId) {
        final var metadataDto = new MetadataDto(publicId);
        metadataDto.populateEmpty();

        return metadataDto;
    }

    private MetadataDto populateDtoFromEntity(TaxonomyEntity taxonomyEntity) {
        final var metadataDto = createEmptyDto(taxonomyEntity.getPublicId());

        metadataDto.setCompetenceAims(taxonomyEntity.getCompetenceAims().stream().map(CompetenceAim::getCode)
                .map(MetadataDto.CompetenceAim::new).collect(Collectors.toSet()));

        metadataDto.setVisible(taxonomyEntity.isVisible());

        metadataDto.setCustomFields(customFieldService.getCustomFields(taxonomyEntity).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue())));

        return metadataDto;
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public MetadataDto getMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException {
        publicIdValidator.validatePublicId(publicId);

        return taxonomyEntityService.getTaxonomyEntity(publicId).map(this::populateDtoFromEntity)
                .orElseGet(() -> createEmptyDto(publicId));
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public List<MetadataDto> getMetadataForTaxonomyEntities(Collection<String> publicIds)
            throws InvalidPublicIdException {
        for (String publicId : publicIds) {
            publicIdValidator.validatePublicId(publicId);
        }

        final var entitiesToReturn = new ConcurrentHashMap<String, MetadataDto>();

        taxonomyEntityService.getTaxonomyEntities(publicIds)
                .forEach(entity -> entitiesToReturn.put(entity.getPublicId(), populateDtoFromEntity(entity)));

        // Returns 1:1 from provided publicId list of DTOs either populated from entity or empty
        // default DTO
        return publicIds.parallelStream()
                .map(publicId -> entitiesToReturn.computeIfAbsent(publicId, this::createEmptyDto))
                .collect(Collectors.toList());
    }

    private void mergeCompetenceAims(TaxonomyEntity taxonomyEntity, Set<MetadataDto.CompetenceAim> competenceAims) {
        final var newCodes = competenceAims.stream().map(MetadataDto.CompetenceAim::getCode).map(String::toUpperCase)
                .collect(Collectors.toSet());

        final var existingCodes = taxonomyEntity.getCompetenceAims().stream().map(CompetenceAim::getCode)
                .map(String::toUpperCase).collect(Collectors.toSet());

        // Add codes that does not exist
        newCodes.stream().filter(newCode -> !existingCodes.contains(newCode))
                .map(competenceAimService::getOrCreateCompetenceAim).forEach(taxonomyEntity::addCompetenceAim);

        // Remove codes not in list
        existingCodes.stream().filter(existingCode -> !newCodes.contains(existingCode))
                .map(competenceAimService::getOrCreateCompetenceAim).forEach(taxonomyEntity::removeCompetenceAim);
    }

    private void mergeEntity(TaxonomyEntity taxonomyEntity, MetadataDto updateDto) {
        if (updateDto.getCompetenceAims() != null) {
            mergeCompetenceAims(taxonomyEntity, updateDto.getCompetenceAims());
        }

        if (updateDto.isVisible() != null) {
            taxonomyEntity.setVisible(updateDto.isVisible());
        }
    }

    private void updateCustomFields(final TaxonomyEntity taxonomyEntity, final MetadataDto updateDto)
            throws InvalidDataException {
        final var customFieldMap = updateDto.getCustomFields();
        if (customFieldMap != null) {
            try {
                final var existingFields = customFieldService.getCustomFields(taxonomyEntity);
                final var removeFields = existingFields.entrySet().stream()
                        .filter(entry -> !customFieldMap.containsKey(entry.getKey())).map(Map.Entry::getValue)
                        .map(CustomFieldService.FieldValue::getId);
                final var setFields = customFieldMap.entrySet().stream().filter(entry -> {
                    final var existing = existingFields.get(entry.getKey());
                    if (existing == null) {
                        return true;
                    }
                    final var existingValue = existing.getValue();
                    if (existingValue == null) {
                        return entry.getValue() != null;
                    }
                    return !existingValue.equals(entry.getValue());
                }).collect(Collectors.toMap(entry -> {
                    final var key = entry.getKey();
                    if (key == null) {
                        throw new CompletionException(new InvalidDataException("Null key for key/value data"));
                    }
                    return key;
                }, entry -> {
                    final var value = entry.getValue();
                    if (value == null) {
                        throw new CompletionException(new InvalidDataException("Null value for key/value data"));
                    }
                    return value;
                }));
                setFields.forEach((key, value) -> customFieldService.setCustomField(taxonomyEntity, key, value));
                removeFields.forEach(id -> {
                    try {
                        customFieldService.unsetCustomField(id);
                    } catch (EntityNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (CompletionException e) {
                final var cause = e.getCause();
                if (cause instanceof InvalidDataException) {
                    throw (InvalidDataException) cause;
                }
                throw e;
            }
        }
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public MetadataDto updateMetadataForTaxonomyEntity(String publicId, MetadataDto updateDto)
            throws InvalidPublicIdException, InvalidDataException {
        var taxonomyEntity = taxonomyEntityService.getOrCreateTaxonomyEntity(publicId);

        mergeEntity(taxonomyEntity, updateDto);

        taxonomyEntity = taxonomyEntityService.saveTaxonomyEntity(taxonomyEntity);

        updateCustomFields(taxonomyEntity, updateDto);

        return getMetadataForTaxonomyEntity(publicId);
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public List<MetadataDto> updateMetadataForTaxonomyEntities(List<MetadataDto> updateDtos)
            throws InvalidPublicIdException, InvalidDataException {
        final var publicIdList = updateDtos.stream().map(MetadataDto::getPublicId).collect(Collectors.toList());

        var index = 0;
        for (final var publicId : publicIdList) {
            ++index;

            if (publicId == null) {
                throw new InvalidPublicIdException("Missing publicId on object index " + index);
            }

            publicIdValidator.validatePublicId(publicId);
        }

        final var entitiesToUpdate = taxonomyEntityService.getOrCreateTaxonomyEntities(publicIdList).stream()
                .collect(Collectors.toMap(TaxonomyEntity::getPublicId, entity -> entity));

        // Applying all the changes requested and persisting the objects
        final var save = updateDtos.stream().map(updateDto -> {
            final var entity = requireNonNull(entitiesToUpdate.get(updateDto.getPublicId()));
            mergeEntity(entity, updateDto);
            return Map.entry(updateDto, entity);
        }).collect(Collectors.toList());
        try {
            save.forEach(pair -> {
                final var updateDto = pair.getKey();
                var entity = pair.getValue();
                entity = taxonomyEntityService.saveTaxonomyEntity(entity);
                try {
                    updateCustomFields(entity, updateDto);
                } catch (InvalidDataException e) {
                    throw new CompletionException(e);
                }
            });
        } catch (CompletionException e) {
            final var cause = e.getCause();
            if (cause instanceof InvalidDataException) {
                throw (InvalidDataException) cause;
            }
            throw e;
        }

        return getMetadataForTaxonomyEntities(publicIdList);
    }

    @Override
    public void deleteMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException {
        publicIdValidator.validatePublicId(publicId);

        taxonomyEntityService.deleteTaxonomyEntity(publicId);
    }
}
