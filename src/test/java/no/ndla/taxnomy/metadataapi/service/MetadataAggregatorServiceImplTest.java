package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.EntityNotFoundException;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidDataException;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetadataAggregatorServiceImplTest {
    private TaxonomyEntityService taxonomyEntityService;
    private CompetenceAimService competenceAimService;
    private CustomFieldService customFieldService;
    private PublicIdValidator publicIdValidator;
    private MetadataAggregatorServiceImpl metadataAggregatorService;

    @BeforeEach
    void setUp() {
        taxonomyEntityService = mock(TaxonomyEntityService.class);
        competenceAimService = mock(CompetenceAimService.class);
        customFieldService = mock(CustomFieldService.class);
        publicIdValidator = mock(PublicIdValidator.class);

        when(taxonomyEntityService.saveTaxonomyEntity(Mockito.any()))
                .thenAnswer(inv -> inv.getArgument(0));

        metadataAggregatorService =
                new MetadataAggregatorServiceImpl(
                        taxonomyEntityService,
                        competenceAimService,
                        customFieldService,
                        publicIdValidator);
    }

    @Test
    void getMetadataForTaxonomyEntity() throws InvalidPublicIdException {
        {
            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getPublicId()).thenReturn("urn:test:1");
            when(taxonomyEntityService.getTaxonomyEntity("urn:test:1"))
                    .thenReturn(Optional.of(taxonomyEntity));
            when(customFieldService.getCustomFields(taxonomyEntity)).thenReturn(Map.of());

            final var metadataDto =
                    metadataAggregatorService.getMetadataForTaxonomyEntity("urn:test:1");

            assertEquals("urn:test:1", metadataDto.getPublicId());
            assertNotNull(metadataDto.getCompetenceAims());
            assertEquals(0, metadataDto.getCompetenceAims().size());
            assertNotNull(metadataDto.getCustomFields());
            assertTrue(metadataDto.getCustomFields().isEmpty());

            verify(publicIdValidator).validatePublicId("urn:test:1");
        }

        {
            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getPublicId()).thenReturn("urn:test:2");
            when(taxonomyEntityService.getTaxonomyEntity("urn:test:2"))
                    .thenReturn(Optional.of(taxonomyEntity));
            when(customFieldService.getCustomFields(taxonomyEntity))
                    .thenReturn(
                            Map.of(
                                    "test",
                                    new CustomFieldService.FieldValue() {
                                        @Override
                                        public UUID getId() {
                                            return UUID.randomUUID();
                                        }

                                        @Override
                                        public String getValue() {
                                            return "value";
                                        }
                                    }));

            final var aim1 = mock(CompetenceAim.class);
            when(aim1.getCode()).thenReturn("AIM1");
            final var aim2 = mock(CompetenceAim.class);
            when(aim2.getCode()).thenReturn("AIM2");

            when(taxonomyEntity.getCompetenceAims()).thenReturn(Set.of(aim1, aim2));

            final var metadataDto =
                    metadataAggregatorService.getMetadataForTaxonomyEntity("urn:test:2");

            assertEquals("urn:test:2", metadataDto.getPublicId());
            assertNotNull(metadataDto.getCompetenceAims());
            assertEquals(2, metadataDto.getCompetenceAims().size());

            assertTrue(
                    metadataDto.getCompetenceAims().stream()
                            .map(MetadataDto.CompetenceAim::getCode)
                            .collect(Collectors.toSet())
                            .containsAll(Set.of("AIM1", "AIM2")));

            assertNotNull(metadataDto.getCustomFields());
            assertEquals("value", metadataDto.getCustomFields().get("test"));

            verify(publicIdValidator).validatePublicId("urn:test:2");
        }
    }

    @Test
    void updateMetadataForTaxonomyEntity() throws InvalidPublicIdException, InvalidDataException {
        // Test merging with existing entries
        {
            final var aimToKeep1 = mock(CompetenceAim.class);
            when(aimToKeep1.getCode()).thenReturn("K1");
            final var aimToKeep2 = mock(CompetenceAim.class);
            when(aimToKeep2.getCode()).thenReturn("K2");
            final var fieldToKeep1 = UUID.randomUUID();
            final var fieldToKeep2 = UUID.randomUUID();

            final var aimToRemove1 = mock(CompetenceAim.class);
            when(aimToRemove1.getCode()).thenReturn("R1");
            final var aimToRemove2 = mock(CompetenceAim.class);
            when(aimToRemove2.getCode()).thenReturn("R2");
            final var fieldToRemove = UUID.randomUUID();

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
            when(taxonomyEntity.getCompetenceAims())
                    .thenReturn(Set.of(aimToKeep1, aimToKeep2, aimToRemove1, aimToRemove2));
            when(taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:4001"))
                    .thenReturn(taxonomyEntity);
            when(customFieldService.getCustomFields(taxonomyEntity))
                    .thenReturn(
                            Map.of(
                                    "keep1",
                                            new CustomFieldServiceImpl.FieldValueImpl(
                                                    fieldToKeep1, "keep-value"),
                                    "keep2",
                                            new CustomFieldServiceImpl.FieldValueImpl(
                                                    fieldToKeep2, "keep-value"),
                                    "remove",
                                            new CustomFieldServiceImpl.FieldValueImpl(
                                                    fieldToRemove, "remove-value")));

            final var requestObject = new MetadataDto("urn:test:4001");
            requestObject.setCompetenceAims(
                    Set.of(
                            new MetadataDto.CompetenceAim("K1"),
                            new MetadataDto.CompetenceAim("K2"),
                            new MetadataDto.CompetenceAim("A1"),
                            new MetadataDto.CompetenceAim("A2")));
            requestObject.setCustomFields(
                    Map.of(
                            "keep1",
                            "changed-keep-value",
                            "keep2",
                            "keep-value",
                            "add",
                            "add-value"));
            metadataAggregatorService.updateMetadataForTaxonomyEntity(
                    "urn:test:4001", requestObject);

            verify(taxonomyEntity).addCompetenceAim(aimToAdd1);
            verify(taxonomyEntity).addCompetenceAim(aimToAdd2);
            verify(taxonomyEntity).removeCompetenceAim(aimToRemove1);
            verify(taxonomyEntity).removeCompetenceAim(aimToRemove2);
            verify(customFieldService, times(1))
                    .setCustomField(taxonomyEntity, "keep1", "changed-keep-value");
            verify(customFieldService, never())
                    .setCustomField(
                            Mockito.eq(taxonomyEntity), Mockito.eq("keep2"), Mockito.anyString());
            verify(customFieldService, times(1)).setCustomField(taxonomyEntity, "add", "add-value");
            try {
                verify(customFieldService, never()).unsetCustomField(fieldToKeep1);
                verify(customFieldService, never()).unsetCustomField(fieldToKeep2);
                verify(customFieldService, times(1)).unsetCustomField(fieldToRemove);
            } catch (EntityNotFoundException e) {
                throw new RuntimeException(e);
            }
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
            when(taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:4002"))
                    .thenReturn(taxonomyEntity);

            final var requestObject = new MetadataDto("urn:test:4001");
            assertNull(requestObject.getCompetenceAims());

            metadataAggregatorService.updateMetadataForTaxonomyEntity(
                    "urn:test:4002", requestObject);

            verify(taxonomyEntity, times(0)).addCompetenceAim(any());
            verify(taxonomyEntity, times(0)).removeCompetenceAim(any());
            verify(customFieldService, never()).getCustomFields(taxonomyEntity);
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
            when(taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:4003"))
                    .thenReturn(taxonomyEntity);

            when(customFieldService.getCustomFields(taxonomyEntity)).thenReturn(Map.of());

            final var requestObject = new MetadataDto("urn:test:4001");
            requestObject.setCompetenceAims(
                    Set.of(
                            new MetadataDto.CompetenceAim("A1"),
                            new MetadataDto.CompetenceAim("A2")));
            requestObject.setCustomFields(Map.of("add", "add-value"));

            metadataAggregatorService.updateMetadataForTaxonomyEntity(
                    "urn:test:4003", requestObject);

            verify(taxonomyEntity).addCompetenceAim(aimToAdd1);
            verify(taxonomyEntity).addCompetenceAim(aimToAdd2);
            verify(taxonomyEntity, times(0)).removeCompetenceAim(any());
            verify(customFieldService, times(1)).setCustomField(taxonomyEntity, "add", "add-value");
        }

        // Test setting visible flag
        // Test merging with no existing entries
        {
            final var taxonomyEntity = mock(TaxonomyEntity.class);
            when(taxonomyEntity.getCompetenceAims()).thenReturn(Set.of());
            when(taxonomyEntityService.getOrCreateTaxonomyEntity("urn:test:4004"))
                    .thenReturn(taxonomyEntity);

            final var requestObject = new MetadataDto("urn:test:4004");

            metadataAggregatorService.updateMetadataForTaxonomyEntity(
                    "urn:test:4004", requestObject);

            verify(taxonomyEntity, times(0)).setVisible(any(Boolean.class));

            requestObject.setVisible(false);

            metadataAggregatorService.updateMetadataForTaxonomyEntity(
                    "urn:test:4004", requestObject);

            verify(taxonomyEntity).setVisible(false);
        }
    }

    @Test
    void deleteMetadataForTaxonomyEntity() throws InvalidPublicIdException {
        doThrow(new InvalidPublicIdException(""))
                .when(publicIdValidator)
                .validatePublicId("urn:test:2");

        {
            verifyNoInteractions(taxonomyEntityService);
            verifyNoInteractions(publicIdValidator);

            metadataAggregatorService.deleteMetadataForTaxonomyEntity("urn:test:1");

            verify(taxonomyEntityService).deleteTaxonomyEntity("urn:test:1");
            verify(publicIdValidator).validatePublicId("urn:test:1");
        }

        try {
            metadataAggregatorService.deleteMetadataForTaxonomyEntity("urn:test:2");

            fail("Expected InvalidPublicIdException");
        } catch (InvalidPublicIdException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void getMetadataForTaxonomyEntities() throws InvalidPublicIdException {
        final var entity1 = mock(TaxonomyEntity.class);
        when(entity1.getPublicId()).thenReturn("urn:test:1");
        when(entity1.isVisible()).thenReturn(true);
        final var entity3 = mock(TaxonomyEntity.class);
        when(entity3.getPublicId()).thenReturn("urn:test:3");
        when(entity3.isVisible()).thenReturn(false);

        final var aim1 = mock(CompetenceAim.class);
        when(aim1.getCode()).thenReturn("A1");
        final var aim2 = mock(CompetenceAim.class);
        when(aim2.getCode()).thenReturn("A2");

        when(entity1.getCompetenceAims()).thenReturn(Set.of(aim1, aim2));

        when(customFieldService.getCustomFields(entity1))
                .thenReturn(
                        Map.of(
                                "test",
                                new CustomFieldServiceImpl.FieldValueImpl(
                                        UUID.randomUUID(), "value")));
        when(customFieldService.getCustomFields(entity3)).thenReturn(Map.of());

        when(taxonomyEntityService.getTaxonomyEntities(any(Collection.class)))
                .thenAnswer(
                        invocationOnMock -> {
                            final var requestList =
                                    (Collection<String>)
                                            invocationOnMock.getArgument(0, Collection.class);

                            assertEquals(3, requestList.size());
                            assertTrue(
                                    requestList.containsAll(
                                            Set.of("urn:test:1", "urn:test:2", "urn:test:3")));

                            return List.of(entity1, entity3);
                        });

        final var returned =
                metadataAggregatorService.getMetadataForTaxonomyEntities(
                        Set.of("urn:test:1", "urn:test:2", "urn:test:3"));

        verify(publicIdValidator).validatePublicId("urn:test:1");
        verify(publicIdValidator).validatePublicId("urn:test:2");
        verify(publicIdValidator).validatePublicId("urn:test:3");

        assertEquals(3, returned.size());

        for (var dto : returned) {
            switch (dto.getPublicId()) {
                case "urn:test:1":
                    assertTrue(dto.isVisible());
                    assertEquals(2, dto.getCompetenceAims().size());
                    assertTrue(
                            dto.getCompetenceAims().stream()
                                    .map(MetadataDto.CompetenceAim::getCode)
                                    .collect(Collectors.toSet())
                                    .containsAll(Set.of("A1", "A2")));
                    assertNotNull(dto.getCustomFields());
                    assertEquals("value", dto.getCustomFields().get("test"));
                    break;
                case "urn:test:2":
                    assertTrue(dto.isVisible());
                    assertEquals(0, dto.getCompetenceAims().size());
                    assertNotNull(dto.getCustomFields());
                    assertTrue(dto.getCustomFields().isEmpty());
                    break;
                case "urn:test:3":
                    assertFalse(dto.isVisible());
                    assertEquals(0, dto.getCompetenceAims().size());
                    assertNotNull(dto.getCustomFields());
                    assertTrue(dto.getCustomFields().isEmpty());
                    break;
                default:
                    fail("Unexpected publicId");
                    break;
            }
        }
    }

    @Test
    void updateMetadataForTaxonomyEntities() throws InvalidPublicIdException, InvalidDataException {
        final var aim1 = mock(CompetenceAim.class);
        when(aim1.getCode()).thenReturn("A1");
        final var aim2 = mock(CompetenceAim.class);
        when(aim2.getCode()).thenReturn("A2");
        final var aim3 = mock(CompetenceAim.class);
        when(aim3.getCode()).thenReturn("A3");

        when(competenceAimService.getOrCreateCompetenceAim("A1")).thenReturn(aim1);
        when(competenceAimService.getOrCreateCompetenceAim("A2")).thenReturn(aim2);
        when(competenceAimService.getOrCreateCompetenceAim("A3")).thenReturn(aim3);

        final var entity1PublicId = randomUUID().toString();
        final var entity2PublicId = randomUUID().toString();
        final var entity3PublicId = randomUUID().toString();

        final var entity1 = new TaxonomyEntity();
        entity1.addCompetenceAim(aim1);
        entity1.addCompetenceAim(aim2);
        entity1.setVisible(false);
        entity1.setPublicId(entity1PublicId);

        final var entity2 = new TaxonomyEntity();
        entity2.addCompetenceAim(aim2);
        entity2.setVisible(false);
        entity2.setPublicId(entity2PublicId);

        final var entity3 = new TaxonomyEntity();
        entity3.addCompetenceAim(aim3);
        entity3.setVisible(true);
        entity3.setPublicId(entity3PublicId);

        when(taxonomyEntityService.getTaxonomyEntities(anyCollection()))
                .thenAnswer(
                        invocationOnMock -> {
                            @SuppressWarnings("unchecked")
                            final var requestedPublicIds =
                                    (Collection<String>) invocationOnMock.getArgument(0);

                            return requestedPublicIds.stream()
                                    .map(
                                            publicId -> {
                                                if (publicId.equals(entity1PublicId)) {
                                                    return entity1;
                                                } else if (publicId.equals(entity2PublicId)) {
                                                    return entity2;
                                                } else if (publicId.equals(entity3PublicId)) {
                                                    return entity3;
                                                }

                                                throw new RuntimeException(
                                                        "Unknown test entity publicId (failure in test!)");
                                            })
                                    .collect(Collectors.toList());
                        });

        when(taxonomyEntityService.getOrCreateTaxonomyEntities(anyCollection()))
                .thenAnswer(
                        invocationOnMock -> {
                            @SuppressWarnings("unchecked")
                            final var requestedPublicIds =
                                    (Collection<String>) invocationOnMock.getArgument(0);

                            return requestedPublicIds.stream()
                                    .map(
                                            publicId -> {
                                                if (publicId.equals(entity1PublicId)) {
                                                    return entity1;
                                                } else if (publicId.equals(entity2PublicId)) {
                                                    return entity2;
                                                } else if (publicId.equals(entity3PublicId)) {
                                                    return entity3;
                                                }

                                                throw new RuntimeException(
                                                        "Unknown test entity publicId (failure in test!)");
                                            })
                                    .collect(Collectors.toList());
                        });

        {
            final var update1 = new MetadataDto();
            update1.setPublicId(entity1PublicId);
            update1.setVisible(false);
            update1.setCompetenceAims(
                    Set.of(
                            new MetadataDto.CompetenceAim("A1"),
                            new MetadataDto.CompetenceAim("A3")));
            update1.setCustomFields(Map.of("test", "value"));

            final var update2 = new MetadataDto();
            update2.setPublicId(entity2PublicId);

            update2.setCompetenceAims(Set.of());
            update2.setVisible(true);
            update2.setCustomFields(Map.of());

            final var update3 = new MetadataDto();
            update3.setPublicId(entity3PublicId);
            update3.setVisible(true);

            final var returnedDtos =
                    metadataAggregatorService.updateMetadataForTaxonomyEntities(
                            List.of(update1, update2, update3));

            assertTrue(entity1.getCompetenceAims().containsAll(Set.of(aim1, aim3)));
            assertEquals(2, entity1.getCompetenceAims().size());
            assertFalse(entity1.isVisible());

            // Overwritten with empty list of objects
            assertEquals(0, entity2.getCompetenceAims().size());
            assertTrue(entity2.isVisible());

            // Not overwriting since the request DTO does not contain elements (is null)
            assertEquals(1, entity3.getCompetenceAims().size());
            assertTrue(entity3.getCompetenceAims().contains(aim3));
            assertTrue(entity3.isVisible());

            assertEquals(3, returnedDtos.size());
            assertTrue(
                    returnedDtos.stream()
                            .map(MetadataDto::getPublicId)
                            .collect(Collectors.toSet())
                            .containsAll(
                                    Set.of(entity1PublicId, entity2PublicId, entity3PublicId)));

            verify(publicIdValidator, atLeastOnce()).validatePublicId(entity1PublicId);
            verify(publicIdValidator, atLeastOnce()).validatePublicId(entity2PublicId);
            verify(publicIdValidator, atLeastOnce()).validatePublicId(entity3PublicId);

            verify(customFieldService, times(1)).setCustomField(entity1, "test", "value");
            verify(customFieldService, never())
                    .setCustomField(Mockito.eq(entity2), Mockito.anyString(), Mockito.anyString());
            verify(customFieldService, never())
                    .setCustomField(Mockito.eq(entity2), Mockito.anyString(), Mockito.anyString());
        }

        // Try update with a request DTO without publicId
        {
            final var update1 = new MetadataDto();
            update1.setVisible(false);
            update1.setCompetenceAims(
                    Set.of(
                            new MetadataDto.CompetenceAim("A1"),
                            new MetadataDto.CompetenceAim("A3")));

            try {
                metadataAggregatorService.updateMetadataForTaxonomyEntities(List.of(update1));
                fail("Expected InvalidPublicIdException");
            } catch (InvalidPublicIdException ignored) {

            }
        }
    }
}
