package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.data.repository.TaxonomyEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

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
}