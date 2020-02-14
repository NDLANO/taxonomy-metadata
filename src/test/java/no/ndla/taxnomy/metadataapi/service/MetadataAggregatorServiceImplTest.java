package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetadataAggregatorServiceImplTest {
    private TaxonomyEntityService taxonomyEntityService;
    private CompetenceAimService competenceAimService;
    private PublicIdValidator publicIdValidator;
    private MetadataAggregatorServiceImpl metadataAggregatorService;

    @BeforeEach
    void setUp() {
        taxonomyEntityService = mock(TaxonomyEntityService.class);
        competenceAimService = mock(CompetenceAimService.class);
        publicIdValidator = mock(PublicIdValidator.class);

        metadataAggregatorService = new MetadataAggregatorServiceImpl(taxonomyEntityService, competenceAimService, publicIdValidator);
    }

    @Test
    void getMetadataForTaxonomyEntity() throws InvalidPublicIdException {
        {
            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getPublicId()).thenReturn("urn:test:1");
            when(taxonomyEntityService.getTaxonomyEntity("urn:test:1")).thenReturn(Optional.of(taxonomyEntity));

            final var metadataDto = metadataAggregatorService.getMetadataForTaxonomyEntity("urn:test:1");

            assertEquals("urn:test:1", metadataDto.getPublicId());
            assertNotNull(metadataDto.getCompetenceAims());
            assertEquals(0, metadataDto.getCompetenceAims().size());

            verify(publicIdValidator).validatePublicId("urn:test:1");
        }

        {
            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getPublicId()).thenReturn("urn:test:2");
            when(taxonomyEntityService.getTaxonomyEntity("urn:test:2")).thenReturn(Optional.of(taxonomyEntity));

            final var aim1 = mock(CompetenceAim.class);
            when(aim1.getCode()).thenReturn("AIM1");
            final var aim2 = mock(CompetenceAim.class);
            when(aim2.getCode()).thenReturn("AIM2");

            when(taxonomyEntity.getCompetenceAims()).thenReturn(Set.of(aim1, aim2));

            final var metadataDto = metadataAggregatorService.getMetadataForTaxonomyEntity("urn:test:2");

            assertEquals("urn:test:2", metadataDto.getPublicId());
            assertNotNull(metadataDto.getCompetenceAims());
            assertEquals(2, metadataDto.getCompetenceAims().size());

            assertTrue(
                    metadataDto
                            .getCompetenceAims()
                            .stream()
                            .map(MetadataDto.CompetenceAim::getCode)
                            .collect(Collectors.toSet())
                            .containsAll(Set.of("AIM1", "AIM2"))
            );

            verify(publicIdValidator).validatePublicId("urn:test:2");
        }
    }

    @Test
    void updateMetadataForTaxonomyEntity() throws InvalidPublicIdException {
        // Test merging with existing entries
        {
            final var aimToKeep1 = mock(CompetenceAim.class);
            when(aimToKeep1.getCode()).thenReturn("K1");
            final var aimToKeep2 = mock(CompetenceAim.class);
            when(aimToKeep2.getCode()).thenReturn("K2");

            final var aimToRemove1 = mock(CompetenceAim.class);
            when(aimToRemove1.getCode()).thenReturn("R1");
            final var aimToRemove2 = mock(CompetenceAim.class);
            when(aimToRemove2.getCode()).thenReturn("R2");

            final var aimToAdd1 = mock(CompetenceAim.class);
            when(aimToAdd1.getCode()).thenReturn("A1");
            final var aimToAdd2 = mock(CompetenceAim.class);
            when(aimToAdd2.getCode()).thenReturn("A2");

            when(competenceAimService.getOrCreateCompetenceAim("A1")).thenReturn(aimToAdd1);
            when(competenceAimService.getOrCreateCompetenceAim("A2")).thenReturn(aimToAdd2);
            when(competenceAimService.getOrCreateCompetenceAim("R1")).thenReturn(aimToRemove1);
            when(competenceAimService.getOrCreateCompetenceAim("R2")).thenReturn(aimToRemove2);
            when(competenceAimService.getOrCreateCompetenceAim("K1")).thenReturn(aimToKeep1);
            when(competenceAimService.getOrCreateCompetenceAim("K2")).thenReturn(aimToKeep2);


            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getCompetenceAims()).thenReturn(Set.of(aimToKeep1, aimToKeep2, aimToRemove1, aimToRemove2));
            when(taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:4001")).thenReturn(taxonomyEntity);

            final var requestObject = new MetadataDto("urn:test:4001");
            requestObject.addCompetenceAim(new MetadataDto.CompetenceAim("K1"));
            requestObject.addCompetenceAim(new MetadataDto.CompetenceAim("K2"));
            requestObject.addCompetenceAim(new MetadataDto.CompetenceAim("A1"));
            requestObject.addCompetenceAim(new MetadataDto.CompetenceAim("A2"));

            metadataAggregatorService.updateMetadataForTaxonomyEntity("urn:test:4001", requestObject);

            verify(taxonomyEntity).addCompetenceAim(aimToAdd1);
            verify(taxonomyEntity).addCompetenceAim(aimToAdd2);
            verify(taxonomyEntity).removeCompetenceAim(aimToRemove1);
            verify(taxonomyEntity).removeCompetenceAim(aimToRemove2);
        }
        // Test with no aims set in request, supposed to do no change
        {
            final var aimToKeep1 = mock(CompetenceAim.class);
            when(aimToKeep1.getCode()).thenReturn("K1");
            final var aimToKeep2 = mock(CompetenceAim.class);
            when(aimToKeep2.getCode()).thenReturn("K2");

            when(competenceAimService.getOrCreateCompetenceAim("K1")).thenReturn(aimToKeep1);
            when(competenceAimService.getOrCreateCompetenceAim("K2")).thenReturn(aimToKeep2);


            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getCompetenceAims()).thenReturn(Set.of(aimToKeep1, aimToKeep2));
            when(taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:4002")).thenReturn(taxonomyEntity);

            final var requestObject = new MetadataDto("urn:test:4001");
            assertNull(requestObject.getCompetenceAims());

            metadataAggregatorService.updateMetadataForTaxonomyEntity("urn:test:4002", requestObject);

            verify(taxonomyEntity, times(0)).addCompetenceAim(any());
            verify(taxonomyEntity, times(0)).removeCompetenceAim(any());
        }

        // Test merging with no existing entries
        {
            final var aimToAdd1 = mock(CompetenceAim.class);
            when(aimToAdd1.getCode()).thenReturn("A1");
            final var aimToAdd2 = mock(CompetenceAim.class);
            when(aimToAdd2.getCode()).thenReturn("A2");

            when(competenceAimService.getOrCreateCompetenceAim("A1")).thenReturn(aimToAdd1);
            when(competenceAimService.getOrCreateCompetenceAim("A2")).thenReturn(aimToAdd2);


            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getCompetenceAims()).thenReturn(Set.of());
            when(taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:4003")).thenReturn(taxonomyEntity);

            final var requestObject = new MetadataDto("urn:test:4001");
            requestObject.addCompetenceAim(new MetadataDto.CompetenceAim("A1"));
            requestObject.addCompetenceAim(new MetadataDto.CompetenceAim("A2"));

            metadataAggregatorService.updateMetadataForTaxonomyEntity("urn:test:4003", requestObject);

            verify(taxonomyEntity).addCompetenceAim(aimToAdd1);
            verify(taxonomyEntity).addCompetenceAim(aimToAdd2);
            verify(taxonomyEntity, times(0)).removeCompetenceAim(any());
        }
    }
}