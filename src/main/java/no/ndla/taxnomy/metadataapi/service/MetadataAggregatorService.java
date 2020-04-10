package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;

import java.util.Set;

public interface MetadataAggregatorService {
    MetadataDto getMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException;
    MetadataDto updateMetadataForTaxonomyEntity(String publicId, MetadataDto updateDto) throws InvalidPublicIdException;

    void deleteMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException;
}
