package no.ndla.taxnomy.metadataapi.data.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class CustomFieldTest {
    private CustomField customField;

    @BeforeEach
    public void setUp() {
        customField = new CustomField();
    }

    @Test
    public void getId() {
        final var id = randomUUID();
        setField(customField, "id", id);
        assertEquals(id, customField.getId());
    }

    @Test
    public void setId() {
        final var id = randomUUID();
        customField.setId(id);
        assertEquals(id, getField(customField, "id"));
    }

    @Test
    public void testPrePersistRandomId() {
        assertNull(customField.getId());
        customField.prePersist();
        assertNotNull(customField.getId());
    }

    @Test
    public void getPublicId() {
        setField(customField, "publicId", "urn:customfield:1");
        assertEquals("urn:customfield:1", customField.getPublicId());
    }

    @Test
    public void setPublicId() {
        customField.setPublicId("urn:customfield:1");
        assertEquals("urn:customfield:1", getField(customField, "publicId"));
    }

    @Test
    public void getKey() {
        setField(customField, "key", "key");
        assertEquals("key", customField.getKey());
    }

    @Test
    public void setKey() {
        customField.setKey("key");
        assertEquals("key", getField(customField, "key"));
    }
}
