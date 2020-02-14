package no.ndla.taxnomy.metadataapi.service.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MetadataDtoTest {
    private MetadataDto metadataDto;

    @BeforeEach
    void setUp() {
        metadataDto = new MetadataDto();
    }

    @Test
    public void testConstructor() {
        final var metadataDto2 = new MetadataDto("urn:test:2");
        assertEquals("urn:test:2", metadataDto2.getPublicId());
    }

    @Test
    void getAndSetPublicId() {
        metadataDto.setPublicId("urn:test:2");
        assertEquals("urn:test:2", metadataDto.getPublicId());
    }

    @Test
    void addAndGetCompetenceAims() {
        final var aim1 = new MetadataDto.CompetenceAim("TEST1");
        final var aim2 = new MetadataDto.CompetenceAim("TEST2");

        assertNull(metadataDto.getCompetenceAims());

        metadataDto.addCompetenceAim(aim1);

        assertEquals(1, metadataDto.getCompetenceAims().size());
        assertTrue(metadataDto.getCompetenceAims().contains(aim1));

        metadataDto.addCompetenceAim(aim2);

        assertEquals(2, metadataDto.getCompetenceAims().size());
        assertTrue(metadataDto.getCompetenceAims().containsAll(Set.of(aim1, aim2)));
    }

    @Test
    void populateEmpty() {
        assertNull(metadataDto.getCompetenceAims());

        metadataDto.populateEmpty();

        assertNotNull(metadataDto.getCompetenceAims());
        assertEquals(0, metadataDto.getCompetenceAims().size());
    }

    @Test
    public void testCompetenceAim() {
        final var competenceAim = new MetadataDto.CompetenceAim();
        assertNull(competenceAim.getCode());
        competenceAim.setCode("TEST");
        assertEquals("TEST", competenceAim.getCode());
    }
}