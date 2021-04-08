package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.service.exception.EntityNotFoundException;

import java.util.Map;
import java.util.UUID;

public interface CustomFieldService {
    void setCustomField(TaxonomyEntity taxonomyEntity, String customField, String value);
    Map<String,FieldValue> getCustomFields(TaxonomyEntity taxonomyEntity);
    void unsetCustomField(UUID id) throws EntityNotFoundException;

    interface FieldValue {
        UUID getId();
        String getValue();
    }
}
