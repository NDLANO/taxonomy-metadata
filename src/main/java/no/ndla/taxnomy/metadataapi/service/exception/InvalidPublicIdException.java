package no.ndla.taxnomy.metadataapi.service.exception;

public class InvalidPublicIdException extends Exception {
    public InvalidPublicIdException(String publicId) {
        super("Invalid publicId " + publicId);
    }
}
