package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.data.repository.TaxonomyEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Service
public class TaxonomyEntityServiceImpl implements TaxonomyEntityService {
    private final TaxonomyEntityRepository taxonomyEntityRepository;

    public TaxonomyEntityServiceImpl(TaxonomyEntityRepository taxonomyEntityRepository) {
        this.taxonomyEntityRepository = taxonomyEntityRepository;
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public Optional<TaxonomyEntity> getTaxonomyEntity(String publicId) {
        return taxonomyEntityRepository.findFirstByPublicId(publicId);
    }

    @Override
    public List<TaxonomyEntity> getTaxonomyEntities(Collection<String> publicIds) {
        if (publicIds.size() == 0) {
            return List.of();
        }

        return taxonomyEntityRepository.findAllByPublicIdInIncludingCompetenceAims(publicIds);
    }

    private TaxonomyEntity createEmptyTaxonomyEntity(String publicId) {
        final var taxonomyNode = new TaxonomyEntity();
        taxonomyNode.setPublicId(publicId);
        return taxonomyNode;
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public TaxonomyEntity getOrCreateTaxonomyEntity(String publicId) {
        return taxonomyEntityRepository.findFirstByPublicId(publicId)
                .orElseGet(() -> taxonomyEntityRepository.saveAndFlush(createEmptyTaxonomyEntity(publicId)));
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public List<TaxonomyEntity> getOrCreateTaxonomyEntities(Collection<String> publicIds) {
        if (publicIds.size() == 0) {
            return List.of();
        }

        final var existingEntities = getTaxonomyEntities(publicIds).stream()
                .collect(Collectors.toMap(TaxonomyEntity::getPublicId, taxonomyEntity -> taxonomyEntity));

        final var entitiesToReturn = publicIds.stream()
                .map(publicId -> existingEntities.computeIfAbsent(publicId, this::createEmptyTaxonomyEntity))
                .collect(Collectors.toList());

        return taxonomyEntityRepository.saveAll(entitiesToReturn);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public TaxonomyEntity saveTaxonomyEntity(TaxonomyEntity taxonomyEntity) {
        return taxonomyEntityRepository.saveAndFlush(taxonomyEntity);
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public void saveTaxonomyEntities(Collection<TaxonomyEntity> taxonomyEntities) {
        taxonomyEntityRepository.saveAll(taxonomyEntities);
        taxonomyEntityRepository.flush();
    }

    @Override
    @Transactional
    public void deleteTaxonomyEntity(String publicId) {
        taxonomyEntityRepository.findFirstByPublicId(publicId).ifPresent(taxonomyEntityRepository::delete);
    }
}
