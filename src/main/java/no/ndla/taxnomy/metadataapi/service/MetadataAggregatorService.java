package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidDataException;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;

import java.util.Collection;
import java.util.List;

public interface MetadataAggregatorService {
    List<MetadataDto> getMetadataForTaxonomyEntities(Collection<String> publicIds) throws InvalidPublicIdException;

    MetadataDto getMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException;

    MetadataDto updateMetadataForTaxonomyEntity(String publicId, MetadataDto updateDto) throws InvalidPublicIdException, InvalidDataException;

    List<MetadataDto> updateMetadataForTaxonomyEntities(List<MetadataDto> updateDtos) throws InvalidPublicIdException, InvalidDataException;

    void deleteMetadataForTaxonomyEntity(String publicId) throws InvalidPublicIdException;
}
