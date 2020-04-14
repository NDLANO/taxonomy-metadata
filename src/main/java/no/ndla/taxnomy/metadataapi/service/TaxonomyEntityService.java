package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaxonomyEntityService {
    Optional<TaxonomyEntity> getTaxonomyEntity(String publicId);

    List<TaxonomyEntity> getTaxonomyEntities(Collection<String> publicIds);

    TaxonomyEntity getOrCreateTaxonomyEntity(String publicId);

    void saveTaxonomyEntity(TaxonomyEntity taxonomyEntity);

    void deleteTaxonomyEntity(String publicId);
}
