package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PublicIdValidatorImplTest {

    @Test
    void validatePublicId() throws InvalidPublicIdException {
        final var validator = new PublicIdValidatorImpl();

        validator.validatePublicId("urn:test:1");

        validator.validatePublicId("urn:test");

        try {
            validator.validatePublicId("");
            fail("Expected InvalidPublicIdException");
        } catch (InvalidPublicIdException ignored) {

        }

        try {
            validator.validatePublicId("test:1");
            fail("Expected InvalidPublicIdException");
        } catch (InvalidPublicIdException ignored) {

        }

        try {
            validator.validatePublicId("http://test.tld");
            fail("Expected InvalidPublicIdException");
        } catch (InvalidPublicIdException ignored) {

        }
    }
}