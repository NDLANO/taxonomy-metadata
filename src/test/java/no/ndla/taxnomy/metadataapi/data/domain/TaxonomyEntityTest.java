package no.ndla.taxnomy.metadataapi.data.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class TaxonomyEntityTest {
    private TaxonomyEntity taxonomyEntity;

    @BeforeEach
    void setUp() {
        taxonomyEntity = new TaxonomyEntity();
    }

    @Test
    void getId() {
        final var id = randomUUID();

        setField(taxonomyEntity, "id", id);
        assertEquals(id, taxonomyEntity.getId());
    }

    @Test
    void getPublicId() {
        setField(taxonomyEntity, "publicId", "urn:test:1");
        assertEquals("urn:test:1", taxonomyEntity.getPublicId());
    }

    @Test
    void setPublicId() {
        taxonomyEntity.setPublicId("urn:test:1");
        assertEquals("urn:test:1", getField(taxonomyEntity, "publicId"));
    }

    @Test
    void addGetAndRemoveCompetenceAim() {
        final var competenceAim1 = mock(CompetenceAim.class);
        final var competenceAim2 = mock(CompetenceAim.class);

        when(competenceAim1.containsTaxonomyEntity(taxonomyEntity)).thenReturn(false);
        when(competenceAim2.containsTaxonomyEntity(taxonomyEntity)).thenReturn(false);

        assertEquals(0, taxonomyEntity.getCompetenceAims().size());

        taxonomyEntity.addCompetenceAim(competenceAim1);

        when(competenceAim1.containsTaxonomyEntity(taxonomyEntity)).thenReturn(true);

        verify(competenceAim1).addTaxonomyEntity(taxonomyEntity);

        assertEquals(1, taxonomyEntity.getCompetenceAims().size());
        assertTrue(taxonomyEntity.getCompetenceAims().contains(competenceAim1));

        taxonomyEntity.addCompetenceAim(competenceAim2);

        when(competenceAim2.containsTaxonomyEntity(taxonomyEntity)).thenReturn(true);

        verify(competenceAim2).addTaxonomyEntity(taxonomyEntity);

        assertEquals(2, taxonomyEntity.getCompetenceAims().size());
        assertTrue(taxonomyEntity.getCompetenceAims().contains(competenceAim1));
        assertTrue(taxonomyEntity.getCompetenceAims().contains(competenceAim2));

        taxonomyEntity.removeCompetenceAim(competenceAim1);

        verify(competenceAim1).removeTaxonomyEntity(taxonomyEntity);

        assertEquals(1, taxonomyEntity.getCompetenceAims().size());
        assertTrue(taxonomyEntity.getCompetenceAims().contains(competenceAim2));

        taxonomyEntity.removeCompetenceAim(competenceAim2);

        verify(competenceAim2).removeTaxonomyEntity(taxonomyEntity);

        assertEquals(0, taxonomyEntity.getCompetenceAims().size());
    }
}