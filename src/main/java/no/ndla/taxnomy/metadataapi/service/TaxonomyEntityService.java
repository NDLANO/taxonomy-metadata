package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;

import java.util.Optional;

public interface TaxonomyEntityService {
    Optional<TaxonomyEntity> getTaxonomyEntity(String publicId);

    TaxonomyEntity getOrCreateTaxonomyEntity(String publicId);

    void saveTaxonomyEntity(TaxonomyEntity taxonomyEntity);

    void deleteTaxonomyEntity(String publicId);
}
