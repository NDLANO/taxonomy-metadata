package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CustomField;
import no.ndla.taxnomy.metadataapi.data.domain.CustomFieldValue;
import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.data.repository.CustomFieldRepository;
import no.ndla.taxnomy.metadataapi.data.repository.CustomFieldValueRepository;
import no.ndla.taxnomy.metadataapi.data.repository.TaxonomyEntityRepository;
import no.ndla.taxnomy.metadataapi.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class CustomFieldServiceImplTest {
    private TaxonomyEntityRepository taxonomyEntityRepository;
    private CustomFieldRepository customFieldRepository;
    private CustomFieldValueRepository customFieldValueRepository;
    private CustomFieldServiceImpl customFieldService;

    @BeforeEach
    public void setUp(@Autowired TaxonomyEntityRepository taxonomyEntityRepository, @Autowired CustomFieldRepository customFieldRepository, @Autowired CustomFieldValueRepository customFieldValueRepository) {
        this.taxonomyEntityRepository = taxonomyEntityRepository;
        this.customFieldRepository = customFieldRepository;
        this.customFieldValueRepository = customFieldValueRepository;
        this.customFieldService = new CustomFieldServiceImpl(customFieldRepository, customFieldValueRepository);
        taxonomyEntityRepository.deleteAll();
        customFieldRepository.deleteAll();
    }

    @AfterAll
    public static void cleanUp(@Autowired TaxonomyEntityRepository taxonomyEntityRepository, @Autowired CustomFieldRepository customFieldRepository, @Autowired CustomFieldValueRepository customFieldValueRepository) {
        taxonomyEntityRepository.deleteAll();
        customFieldRepository.deleteAll();
    }

    @Test
    public void testSetCustomFieldForTheFirstTime() {
        assertFalse(customFieldRepository.findByKey("new-field").isPresent());
        TaxonomyEntity taxonomyEntity = new TaxonomyEntity();
        taxonomyEntity.setPublicId("urn:test:1");
        taxonomyEntity = taxonomyEntityRepository.save(taxonomyEntity);
        assertNotNull(taxonomyEntity.getId());
        assertTrue(customFieldService.getCustomFields(taxonomyEntity).isEmpty());
        customFieldService.setCustomField(taxonomyEntity, "new-field", "A Value");
        final var customField = customFieldRepository.findByKey("new-field").orElse(null);
        assertNotNull(customField);
        assertNotNull(customField.getId());
        final var customFieldValue = customFieldValueRepository.findByTaxonomyEntityAndCustomField(taxonomyEntity.getId(), customField.getId()).orElse(null);
        assertNotNull(customFieldValue);
        assertNotNull(customFieldValue.getId());
        assertNotNull(customFieldValue.getTaxonomyEntity());
        assertNotNull(customFieldValue.getCustomField());
        assertEquals(taxonomyEntity.getId(), customFieldValue.getTaxonomyEntity().getId());
        assertEquals(customField.getId(), customFieldValue.getCustomField().getId());
        assertEquals("new-field", customFieldValue.getCustomField().getKey());
        assertEquals("A Value", customFieldValue.getValue());
    }

    @Test
    public void testSetCustomField() {
        CustomField customField = new CustomField();
        customField.setPublicId("urn:customfield:1");
        customField.setKey("new-field");
        customField = customFieldRepository.save(customField);
        assertNotNull(customField.getId());
        assertNotNull(customField.getCreatedAt());
        TaxonomyEntity taxonomyEntity = new TaxonomyEntity();
        taxonomyEntity.setPublicId("urn:test:1");
        taxonomyEntity = taxonomyEntityRepository.save(taxonomyEntity);
        assertNotNull(taxonomyEntity.getId());
        assertTrue(customFieldService.getCustomFields(taxonomyEntity).isEmpty());
    }

    @Test
    public void testGetCustomFieldValuesAndDelete() throws EntityNotFoundException {
        CustomField customField = new CustomField();
        customField.setPublicId("urn:customfield:1");
        customField.setKey("new-field");
        customField = customFieldRepository.save(customField);
        assertNotNull(customField.getId());
        assertNotNull(customField.getCreatedAt());
        TaxonomyEntity taxonomyEntity = new TaxonomyEntity();
        taxonomyEntity.setPublicId("urn:test:1");
        taxonomyEntity = taxonomyEntityRepository.save(taxonomyEntity);
        assertNotNull(taxonomyEntity.getId());
        CustomFieldValue customFieldValue = new CustomFieldValue();
        customFieldValue.setCustomField(customField);
        customFieldValue.setTaxonomyEntity(taxonomyEntity);
        customFieldValue.setValue("A value");
        customFieldValue = customFieldValueRepository.save(customFieldValue);
        assertNotNull(customFieldValue.getId());
        Map<String, CustomFieldService.FieldValue> values = customFieldService.getCustomFields(taxonomyEntity);
        assertFalse(values.isEmpty());
        final var value = values.get("new-field");
        assertEquals(customFieldValue.getId(), value.getId());
        assertEquals("A value", value.getValue());
        customFieldService.unsetCustomField(value.getId());
        assertTrue(customFieldService.getCustomFields(taxonomyEntity).isEmpty());
    }

    @Test
    public void testDeleteUnknownValue() {
        final var id = UUID.randomUUID();
        assertThrows(EntityNotFoundException.class, () -> {customFieldService.unsetCustomField(id); });
    }

    @Test
    public void testGetByKeyValueKeyNotFound() {
        assertTrue(customFieldService.getTaxonomyEntitiesByCustomFieldKeyValue("testkey", "testvalue").isEmpty());
    }

    @Test
    public void testGetByKeyValueNoValues() {
        CustomField customField = new CustomField();
        customField.setPublicId("urn:customfield:1");
        customField.setKey("testkey");
        customField = customFieldRepository.save(customField);
        assertTrue(customFieldService.getTaxonomyEntitiesByCustomFieldKeyValue("testkey", "testvalue").isEmpty());
    }

    @Test
    public void testGetByKeyValue() {
        {
            CustomField customField = new CustomField();
            customField.setPublicId("urn:customfield:1");
            customField.setKey("testkey");
            customField = customFieldRepository.save(customField);
            TaxonomyEntity taxonomyEntity = new TaxonomyEntity();
            taxonomyEntity.setPublicId("urn:test:1");
            taxonomyEntity = taxonomyEntityRepository.save(taxonomyEntity);
            {
                CustomFieldValue customFieldValue = new CustomFieldValue();
                customFieldValue.setCustomField(customField);
                customFieldValue.setTaxonomyEntity(taxonomyEntity);
                customFieldValue.setValue("testvalue");
                customFieldValueRepository.save(customFieldValue);
            }
        }
        final var entities = customFieldService.getTaxonomyEntitiesByCustomFieldKeyValue("testkey", "testvalue");
        assertFalse(entities.isEmpty());
        final TaxonomyEntity taxonomyEntity;
        {
            final var iterator = entities.iterator();
            assertTrue(iterator.hasNext());
            taxonomyEntity = iterator.next();
            assertFalse(iterator.hasNext());
        }
        assertEquals("urn:test:1", taxonomyEntity.getPublicId().toString());
    }
}
