package no.ndla.taxnomy.metadataapi.data.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class CompetenceAimTest {
    private CompetenceAim competenceAim;

    @BeforeEach
    void setUp() {
        competenceAim = new CompetenceAim();
    }

    @Test
    void getId() {
        final var id = randomUUID();
        setField(competenceAim, "id", id);

        assertEquals(id, competenceAim.getId());
    }

    @Test
    void getCode() {
        setField(competenceAim, "code", "KM4545");
        assertEquals("KM4545", competenceAim.getCode());
    }

    @Test
    void setCode() {
        competenceAim.setCode("KM4646");
        assertEquals("KM4646", getField(competenceAim, "code"));
    }

    @Test
    void addGetRemoveContainsTaxonomyEntity() {
        final var taxonomyEntity1 = mock(TaxonomyEntity.class);
        final var taxonomyEntity2 = mock(TaxonomyEntity.class);

        assertFalse(competenceAim.containsTaxonomyEntity(taxonomyEntity1));
        assertFalse(competenceAim.containsTaxonomyEntity(taxonomyEntity2));
        assertEquals(0, competenceAim.getTaxonomyEntities().size());

        competenceAim.addTaxonomyEntity(taxonomyEntity1);

        assertTrue(competenceAim.containsTaxonomyEntity(taxonomyEntity1));
        assertFalse(competenceAim.containsTaxonomyEntity(taxonomyEntity2));
        assertEquals(1, competenceAim.getTaxonomyEntities().size());

        competenceAim.addTaxonomyEntity(taxonomyEntity2);

        assertTrue(competenceAim.containsTaxonomyEntity(taxonomyEntity1));
        assertTrue(competenceAim.containsTaxonomyEntity(taxonomyEntity2));
        assertEquals(2, competenceAim.getTaxonomyEntities().size());

        assertTrue(
                competenceAim
                        .getTaxonomyEntities()
                        .containsAll(Set.of(taxonomyEntity1, taxonomyEntity2)));

        competenceAim.removeTaxonomyEntity(taxonomyEntity1);

        assertFalse(competenceAim.containsTaxonomyEntity(taxonomyEntity1));
        assertTrue(competenceAim.containsTaxonomyEntity(taxonomyEntity2));
        assertEquals(1, competenceAim.getTaxonomyEntities().size());

        competenceAim.removeTaxonomyEntity(taxonomyEntity2);

        assertFalse(competenceAim.containsTaxonomyEntity(taxonomyEntity1));
        assertFalse(competenceAim.containsTaxonomyEntity(taxonomyEntity2));
        assertEquals(0, competenceAim.getTaxonomyEntities().size());
    }
}
