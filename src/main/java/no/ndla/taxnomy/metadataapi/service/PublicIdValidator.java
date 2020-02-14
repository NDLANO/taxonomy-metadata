package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;

public interface PublicIdValidator {
    void validatePublicId(String publicId) throws InvalidPublicIdException;
}
