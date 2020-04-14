package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class PublicIdValidatorImpl implements PublicIdValidator {
    @Override
    public void validatePublicId(String publicId) throws InvalidPublicIdException {
        if (!publicId.toLowerCase().equals(publicId)) {
            throw new InvalidPublicIdException("ID must only contain lower-case letters");
        }

        try {
            final var uri = new URI(publicId);

            if (uri.getScheme() == null || !uri.getScheme().equals("urn")) {
                throw new InvalidPublicIdException(publicId);
            }
        } catch (URISyntaxException e) {
            throw new InvalidPublicIdException(publicId);
        }
    }
}
