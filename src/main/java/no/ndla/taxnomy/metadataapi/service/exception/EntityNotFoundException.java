package no.ndla.taxnomy.metadataapi.service.exception;

import java.util.UUID;

public class EntityNotFoundException extends Exception {
    public EntityNotFoundException(String id) {
        super("Entity not found: "+id);
    }
    public EntityNotFoundException(UUID id) {
        this(id != null ? id.toString() : "null");
    }
}
