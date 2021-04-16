package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CustomField;
import no.ndla.taxnomy.metadataapi.data.domain.CustomFieldValue;
import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.data.repository.CustomFieldRepository;
import no.ndla.taxnomy.metadataapi.data.repository.CustomFieldValueRepository;
import no.ndla.taxnomy.metadataapi.service.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CustomFieldServiceImpl implements CustomFieldService {
    private CustomFieldRepository customFieldRepository;
    private CustomFieldValueRepository customFieldValueRepository;

    public CustomFieldServiceImpl(CustomFieldRepository customFieldRepository, CustomFieldValueRepository customFieldValueRepository) {
        this.customFieldRepository = customFieldRepository;
        this.customFieldValueRepository = customFieldValueRepository;
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void setCustomField(final TaxonomyEntity taxonomyEntity, final String customField, final String value) {
        final CustomField customFieldObject = customFieldRepository.findByKey(customField).orElseGet(() -> {
            CustomField customFieldObj = new CustomField();
            customFieldObj.setPublicId("urn:customfield:"+UUID.randomUUID().toString());
            customFieldObj.setKey(customField);
            return customFieldRepository.save(customFieldObj);
        });
        final CustomFieldValue valueObject = customFieldValueRepository.findByTaxonomyEntityAndCustomField(taxonomyEntity.getId(), customFieldObject.getId()).orElseGet(() -> {
            CustomFieldValue newObject = new CustomFieldValue();
            newObject.setCustomField(customFieldObject);
            newObject.setTaxonomyEntity(taxonomyEntity);
            return newObject;
        });
        valueObject.setValue(value);
        customFieldValueRepository.save(valueObject);
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public Map<String, FieldValue> getCustomFields(TaxonomyEntity taxonomyEntity) {
        return StreamSupport.stream(customFieldValueRepository.findAllByTaxonomyEntity(taxonomyEntity.getId()).spliterator(), false)
                .collect(Collectors.toMap(value -> value.getCustomField().getKey(), value -> new FieldValueImpl(value.getId(), value.getValue())));
    }

    @Override
    public void unsetCustomField(UUID id) throws EntityNotFoundException {
        customFieldValueRepository.delete(customFieldValueRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(id)));
    }

    @Override
    public List<TaxonomyEntity> getTaxonomyEntitiesByCustomFieldKeyValue(String key, String value) {
        final UUID customFieldId;
        {
            final CustomField customField;
            {
                final var opt = customFieldRepository.findByKey(key);
                if (opt.isEmpty()) {
                    return List.of();
                }
                customField = opt.get();
            }
            customFieldId = customField.getId();
        }
        return StreamSupport.stream(customFieldValueRepository.findAllByCustomFieldAndValue(customFieldId, value).spliterator(), false)
                .map(CustomFieldValue::getTaxonomyEntity)
                .collect(Collectors.toList());
    }

    static class FieldValueImpl implements FieldValue {
        private UUID id;
        private String value;

        FieldValueImpl(UUID id, String value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getValue() {
            return value;
        }
    }
}
