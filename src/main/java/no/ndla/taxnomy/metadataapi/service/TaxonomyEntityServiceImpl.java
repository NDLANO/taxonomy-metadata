package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.data.repository.TaxonomyEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    @Transactional(propagation = MANDATORY)
    public TaxonomyEntity getOrCreateTaxonomyEntity(String publicId) {
        return taxonomyEntityRepository.findFirstByPublicId(publicId).orElseGet(() -> {
            final var taxonomyNode = new TaxonomyEntity();
            taxonomyNode.setPublicId(publicId);
            return taxonomyEntityRepository.saveAndFlush(taxonomyNode);
        });
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public void saveTaxonomyEntity(TaxonomyEntity taxonomyEntity) {
        taxonomyEntityRepository.saveAndFlush(taxonomyEntity);
    }

    @Override
    @Transactional
    public void deleteTaxonomyEntity(String publicId) {
        taxonomyEntityRepository.findFirstByPublicId(publicId)
                .ifPresent(taxonomyEntityRepository::delete);
    }
}
