package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaxonomyEntityService {
    Optional<TaxonomyEntity> getTaxonomyEntity(String publicId);

    List<TaxonomyEntity> getTaxonomyEntities(Collection<String> publicIds);

    TaxonomyEntity getOrCreateTaxonomyEntity(String publicId);

    List<TaxonomyEntity> getOrCreateTaxonomyEntities(Collection<String> publicId);

    TaxonomyEntity saveTaxonomyEntity(TaxonomyEntity taxonomyEntity);

    void saveTaxonomyEntities(Collection<TaxonomyEntity> taxonomyEntities);

    void deleteTaxonomyEntity(String publicId);
}
