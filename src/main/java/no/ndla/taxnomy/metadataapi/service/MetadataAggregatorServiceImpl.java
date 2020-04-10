package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
public class MetadataAggregatorServiceImpl implements MetadataAggregatorService {
    private final TaxonomyEntityService taxonomyEntityService;
    private final CompetenceAimService competenceAimService;
    private final PublicIdValidator publicIdValidator;

    public MetadataAggregatorServiceImpl(TaxonomyEntityService taxonomyEntityService, CompetenceAimService competenceAimService, PublicIdValidator publicIdValidator) {
        this.taxonomyEntityService = taxonomyEntityService;
        this.competenceAimService = competenceAimService;
        this.publicIdValidator = publicIdValidator;
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public MetadataDto getMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException {
        publicIdValidator.validatePublicId(publicId);

        final var metadataDto = new MetadataDto(publicId);

        taxonomyEntityService.getTaxonomyEntity(publicId).stream()
                .map(TaxonomyEntity::getCompetenceAims)
                .flatMap(Collection::stream)
                .map(CompetenceAim::getCode)
                .map(MetadataDto.CompetenceAim::new)
                .forEach(metadataDto::addCompetenceAim);

        metadataDto.populateEmpty();

        return metadataDto;
    }

    private void mergeCompetenceAims(TaxonomyEntity taxonomyEntity, Set<MetadataDto.CompetenceAim> competenceAims) {
        final var newCodes = competenceAims.stream()
                .map(MetadataDto.CompetenceAim::getCode)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        final var existingCodes = taxonomyEntity.getCompetenceAims().stream()
                .map(CompetenceAim::getCode)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());


        // Add codes that does not exist
        newCodes.stream()
                .filter(newCode -> !existingCodes.contains(newCode))
                .map(competenceAimService::getOrCreateCompetenceAim)
                .forEach(taxonomyEntity::addCompetenceAim);

        // Remove codes not in list
        existingCodes.stream()
                .filter(existingCode -> !newCodes.contains(existingCode))
                .map(competenceAimService::getOrCreateCompetenceAim)
                .forEach(taxonomyEntity::removeCompetenceAim);
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public MetadataDto updateMetadataForTaxonomyEntity(String publicId, MetadataDto updateDto) throws InvalidPublicIdException {
        final var taxonomyEntity = taxonomyEntityService.getOrCreateTaxonomyEntity(publicId);

        if (updateDto.getCompetenceAims() != null) {
            mergeCompetenceAims(taxonomyEntity, updateDto.getCompetenceAims());
        }

        taxonomyEntityService.saveTaxonomyEntity(taxonomyEntity);

        return getMetadataForTaxonomyEntity(publicId);
    }

    @Override
    public void deleteMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException {
        publicIdValidator.validatePublicId(publicId);

        taxonomyEntityService.deleteTaxonomyEntity(publicId);
    }
}
