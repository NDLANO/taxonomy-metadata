package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.data.repository.CompetenceAimRepository;
import no.ndla.taxnomy.metadataapi.data.repository.TaxonomyEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TaxonomyEntityServiceImplTest {
    private TaxonomyEntityRepository taxonomyEntityRepository;
    private TaxonomyEntityServiceImpl taxonomyEntityService;

    @BeforeEach
    void setUp(@Autowired TaxonomyEntityRepository taxonomyEntityRepository) {
        this.taxonomyEntityRepository = taxonomyEntityRepository;
        taxonomyEntityService = new TaxonomyEntityServiceImpl(taxonomyEntityRepository);
    }

    @Test
    @Transactional
    void getTaxonomyEntity() {
        assertFalse(taxonomyEntityService.getTaxonomyEntity("urn:test:1300").isPresent());

        final var taxonomyEntity = new TaxonomyEntity();
        taxonomyEntity.setPublicId("urn:test:1300");
        taxonomyEntityRepository.saveAndFlush(taxonomyEntity);

        assertTrue(taxonomyEntityService.getTaxonomyEntity("urn:test:1300").isPresent());
        assertSame(taxonomyEntity, taxonomyEntityService.getTaxonomyEntity("urn:test:1300").orElseThrow());
    }

    @Test
    @Transactional
    void getOrCreateTaxonomyEntity() {
        assertFalse(taxonomyEntityService.getTaxonomyEntity("urn:test:1301").isPresent());

        final var taxonomyEntity1 = taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:1301");

        assertNotNull(taxonomyEntity1);
        assertNotNull(taxonomyEntity1.getId());
        assertEquals("urn:test:1301", taxonomyEntity1.getPublicId());

        assertTrue(taxonomyEntityService.getTaxonomyEntity("urn:test:1301").isPresent());
        assertSame(taxonomyEntity1, taxonomyEntityService.getTaxonomyEntity("urn:test:1301").orElseThrow());

        assertFalse(taxonomyEntityService.getTaxonomyEntity("urn:test:1302").isPresent());

        final var taxonomyEntity2 = new TaxonomyEntity();
        taxonomyEntity2.setPublicId("urn:test:1302");
        taxonomyEntityRepository.saveAndFlush(taxonomyEntity2);

        assertTrue(taxonomyEntityService.getTaxonomyEntity("urn:test:1302").isPresent());
        assertSame(taxonomyEntity2, taxonomyEntityService.getTaxonomyEntity("urn:test:1302").orElseThrow());
    }

    @Test
    void saveTaxonomyEntity() {
        final var taxonomyEntity = new TaxonomyEntity();
        taxonomyEntity.setPublicId("urn:test:1303");

        assertNull(taxonomyEntity.getId());

        assertFalse(taxonomyEntityRepository.findFirstByPublicId("urn:test:1303").isPresent());

        taxonomyEntityService.saveTaxonomyEntity(taxonomyEntity);

        assertNotNull(taxonomyEntity.getId());

        assertTrue(taxonomyEntityRepository.findFirstByPublicId("urn:test:1303").isPresent());
    }

    @Test
    @Transactional
    void deleteTaxonomyEntity(@Autowired CompetenceAimRepository competenceAimRepository) {
        final var taxonomyEntity1 = new TaxonomyEntity();
        taxonomyEntity1.setPublicId("urn:test:34");
        taxonomyEntityRepository.saveAndFlush(taxonomyEntity1);

        final var taxonomyEntity2 = new TaxonomyEntity();
        taxonomyEntity2.setPublicId("urn:test:35");
        taxonomyEntityRepository.saveAndFlush(taxonomyEntity2);

        final var competenceAim1 = new CompetenceAim();
        competenceAim1.setCode("A1");
        final var competenceAim2 = new CompetenceAim();
        competenceAim2.setCode("A2");

        taxonomyEntity1.addCompetenceAim(competenceAim1);
        taxonomyEntity1.addCompetenceAim(competenceAim2);
        taxonomyEntity2.addCompetenceAim(competenceAim2);

        competenceAimRepository.saveAll(Set.of(competenceAim1, competenceAim2));

        assertNotNull(competenceAim1.getId());
        assertNotNull(competenceAim2.getId());

        final var aim1Id = competenceAim1.getId();
        final var aim2Id = competenceAim2.getId();
        final var entityId1 = taxonomyEntity1.getId();
        final var entityId2 = taxonomyEntity2.getId();

        assertTrue(competenceAimRepository.findById(aim1Id).isPresent());
        assertTrue(competenceAimRepository.findById(aim2Id).isPresent());
        assertTrue(taxonomyEntityRepository.findById(entityId1).isPresent());

        assertEquals(1, competenceAim1.getTaxonomyEntities().size());
        assertEquals(2, competenceAim2.getTaxonomyEntities().size());
        assertTrue(competenceAim1.getTaxonomyEntities().contains(taxonomyEntity1));
        assertTrue(competenceAim2.getTaxonomyEntities().containsAll(Set.of(taxonomyEntity1, taxonomyEntity2)));

        taxonomyEntityService.deleteTaxonomyEntity("urn:test:34");

        assertTrue(competenceAimRepository.findById(aim2Id).isPresent());

        assertFalse(taxonomyEntityRepository.findById(entityId1).isPresent());
        assertTrue(taxonomyEntityRepository.findById(entityId2).isPresent());

        assertEquals(0, competenceAim1.getTaxonomyEntities().size());
        assertEquals(1, competenceAim2.getTaxonomyEntities().size());
        assertTrue(competenceAim2.getTaxonomyEntities().contains(taxonomyEntity2));

        taxonomyEntityService.deleteTaxonomyEntity("urn:test:35");

        assertEquals(0, competenceAim1.getTaxonomyEntities().size());
        assertEquals(0, competenceAim2.getTaxonomyEntities().size());

        assertFalse(taxonomyEntityRepository.findById(entityId2).isPresent());
    }

    @Test
    @Transactional
    void getTaxonomyEntities() {
        final var entity1 = new TaxonomyEntity();
        entity1.setPublicId("urn:test:1");
        entity1.setVisible(true);

        final var entity2 = new TaxonomyEntity();
        entity2.setPublicId("urn:test:2");
        entity2.setVisible(false);

        final var entity3 = new TaxonomyEntity();
        entity3.setPublicId("urn:test:3");
        entity3.setVisible(true);

        taxonomyEntityRepository.saveAll(Set.of(entity1, entity2, entity3));

        final var returned1 = taxonomyEntityService.getTaxonomyEntities(Set.of("urn:test:1", "urn:test:3"));
        assertEquals(2, returned1.size());
        assertTrue(returned1.containsAll(Set.of(entity1, entity3)));

        final var returned2 = taxonomyEntityService.getTaxonomyEntities(Set.of());
        assertEquals(0, returned2.size());

        final var returned3 = taxonomyEntityService.getTaxonomyEntities(Set.of("urn:test:1", "urn:test:4"));
        assertEquals(1, returned3.size());
        assertTrue(returned3.contains(entity1));
    }
}