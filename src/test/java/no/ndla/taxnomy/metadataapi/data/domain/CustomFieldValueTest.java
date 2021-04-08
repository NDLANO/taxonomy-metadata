package no.ndla.taxnomy.metadataapi.data.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class CustomFieldValueTest {
    private CustomFieldValue customFieldValue;

    @BeforeEach
    public void setUp() {
        customFieldValue = new CustomFieldValue();
    }

    @Test
    public void getId() {
        final var id = randomUUID();
        setField(customFieldValue, "id", id);
        assertEquals(id, customFieldValue.getId());
    }

    @Test
    public void setId() {
        final var id = randomUUID();
        customFieldValue.setId(id);
        assertEquals(id, getField(customFieldValue, "id"));
    }

    @Test
    public void testPrePersistRandomId() {
        assertNull(customFieldValue.getId());
        customFieldValue.prePersist();
        assertNotNull(customFieldValue);
    }

    @Test
    public void getTaxonomyEntity() {
        final var taxonomyEntity = new TaxonomyEntity();
        setField(customFieldValue, "taxonomyEntity", taxonomyEntity);
        assertSame(taxonomyEntity, customFieldValue.getTaxonomyEntity());
    }

    @Test
    public void setTaxonomyEntity() {
        final var taxonomyEntity = new TaxonomyEntity();
        customFieldValue.setTaxonomyEntity(taxonomyEntity);
        assertSame(taxonomyEntity, getField(customFieldValue, "taxonomyEntity"));
    }

    @Test
    public void getCustomField() {
        final var customField = new CustomField();
        setField(customFieldValue, "customField", customField);
        assertSame(customField, customFieldValue.getCustomField());
    }

    @Test
    public void setCustomField() {
        final var customField = new CustomField();
        customFieldValue.setCustomField(customField);
        assertSame(customField, getField(customFieldValue, "customField"));
    }

    @Test
    public void getValue() {
        setField(customFieldValue, "value", "value");
        assertEquals("value", customFieldValue.getValue());
    }

    @Test
    public void setValue() {
        customFieldValue.setValue("value");
        assertEquals("value", customFieldValue.getValue());
    }
}
