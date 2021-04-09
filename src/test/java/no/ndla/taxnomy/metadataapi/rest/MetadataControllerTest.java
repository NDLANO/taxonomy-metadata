package no.ndla.taxnomy.metadataapi.rest;

import no.ndla.taxnomy.metadataapi.rest.exception.InvalidRequestException;
import no.ndla.taxnomy.metadataapi.service.MetadataAggregatorService;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidDataException;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MetadataControllerTest {
    private MetadataAggregatorService metadataAggregatorService;
    private MetadataController metadataController;

    @BeforeEach
    void setUp() {
        metadataAggregatorService = mock(MetadataAggregatorService.class);
        metadataController = new MetadataController(metadataAggregatorService);
    }

    @Test
    void get() throws InvalidPublicIdException {
        {
            final var metadataDtoToReturn = mock(MetadataDto.class);
            final var competenceAimToReturn1 = mock(MetadataDto.CompetenceAim.class);
            when(competenceAimToReturn1.getCode()).thenReturn("AIM1");
            final var competenceAimToReturn2 = mock(MetadataDto.CompetenceAim.class);
            when(competenceAimToReturn2.getCode()).thenReturn("AIM2");

            final var competenceAimsToReturn = Set.of(competenceAimToReturn1, competenceAimToReturn2);

            when(metadataDtoToReturn.getPublicId()).thenReturn("urn:test:1");
            when(metadataDtoToReturn.getCompetenceAims()).thenReturn(competenceAimsToReturn);

            when(metadataAggregatorService.getMetadataForTaxonomyEntity("urn:test:1")).thenReturn(metadataDtoToReturn);
        }

        final var returned = metadataController.get("urn:test:1");
        assertEquals("urn:test:1", returned.getPublicId());

        final var returnedAims = returned.getCompetenceAims();
        assertEquals(2, returned.getCompetenceAims().size());

        assertTrue(returnedAims.stream().map(MetadataDto.CompetenceAim::getCode).collect(Collectors.toSet()).containsAll(Set.of("AIM1", "AIM2")));
    }

    @Test
    void put() throws InvalidPublicIdException, InvalidDataException {
        {
            final var metadataDtoToReturn = mock(MetadataDto.class);
            when(metadataDtoToReturn.getPublicId()).thenReturn("urn:test:1");
            when(metadataDtoToReturn.getCompetenceAims()).thenReturn(Set.of());

            when(metadataAggregatorService.updateMetadataForTaxonomyEntity(eq("urn:test:1"), any(MetadataDto.class))).thenAnswer(invocationOnMock -> {
                final var metadataUpdateObject = invocationOnMock.getArgument(1, MetadataDto.class);

                assertNotNull(metadataUpdateObject);

                return metadataUpdateObject;
            });
        }

        {
            final var bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(false);

            final var aim1 = new MetadataDto.CompetenceAim("AIM1");
            final var aim2 = new MetadataDto.CompetenceAim("AIM2");
            final var requestObject = new MetadataDto("urn:test:1");
            requestObject.setCompetenceAims(Set.of(aim1, aim2));

            final var returnedMetadataDto = metadataController.put("urn:test:1", requestObject, bindingResult);

            assertEquals("urn:test:1", returnedMetadataDto.getPublicId());

            assertTrue(returnedMetadataDto.getCompetenceAims().stream().map(MetadataDto.CompetenceAim::getCode).collect(Collectors.toSet()).containsAll(Set.of("AIM1", "AIM2")));

            verify(metadataAggregatorService).updateMetadataForTaxonomyEntity(eq("urn:test:1"), any(MetadataDto.class));
        }


        {
            final var bindingResult = mock(BindingResult.class);
            when(bindingResult.hasErrors()).thenReturn(true);

            when(bindingResult.getErrorCount()).thenReturn(1);
            when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("name", "message")));

            final var requestObject = new MetadataDto("urn:test:1");

            try {
                metadataController.put("urn:test:1", requestObject, bindingResult);
                fail("Expected InvalidRequestException");
            } catch (InvalidRequestException ignored) {

            }
        }
    }

    @Test
    void delete() throws InvalidPublicIdException {
        doThrow(new InvalidPublicIdException("")).when(metadataAggregatorService).deleteMetadataForTaxonomyEntity("urn:test:2");

        {
            final var returned = metadataController.delete("urn:test:1");
            verify(metadataAggregatorService).deleteMetadataForTaxonomyEntity("urn:test:1");
            assertEquals(HttpStatus.NO_CONTENT, returned.getStatusCode());
        }

        try {
            metadataController.delete("urn:test:2");
            fail("Expected InvalidRequestException");
        } catch (InvalidRequestException ignored) {

        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void getMultiple() throws InvalidPublicIdException {
        {
            final var toReturn = mock(List.class);

            when(metadataAggregatorService.getMetadataForTaxonomyEntities(any(Collection.class))).thenAnswer(invocationOnMock -> {
                final var requested = (Collection<String>) invocationOnMock.getArgument(0, Collection.class);

                assertEquals(3, requested.size());
                assertTrue(requested.containsAll(Set.of("urn:test:1", "urn:test:2", "urn:test:3")));

                return toReturn;
            });

            final var returned = metadataController.getMultiple("urn:test:1,urn:test:2,urn:test:3");
            assertSame(toReturn, returned);

            reset(metadataAggregatorService);
        }

        {
            final var idListBuilder = new StringBuilder();
            for (var i = 0; i < 101; i++) {
                if (i > 0) {
                    idListBuilder.append(",");
                }

                idListBuilder.append("urn:test:");
                idListBuilder.append(i);
            }

            try {
                metadataController.getMultiple(idListBuilder.toString());
                fail("Expected InvalidRequestException");
            } catch (InvalidRequestException ignored) {

            }
        }

        {
            try {
                final var returned = metadataController.getMultiple("");
                assertEquals(0, returned.size());
            } catch (InvalidRequestException ignored) {

            }
        }

        {
            when(metadataAggregatorService.getMetadataForTaxonomyEntities(any(Collection.class))).thenThrow(new InvalidPublicIdException(""));

            try {
                metadataController.getMultiple("urn:test:1");
                fail("Expected InvalidRequestException");
            } catch (InvalidRequestException exception) {
                assertTrue(exception.getCause() instanceof InvalidPublicIdException);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    void putBulk() throws InvalidPublicIdException, InvalidDataException {
        {
            final var entity1ToUpdate = mock(MetadataDto.class);
            final var entity2ToUpdate = mock(MetadataDto.class);

            final var entity1ToReturn = mock(MetadataDto.class);
            final var entity2ToReturn = mock(MetadataDto.class);

            when(metadataAggregatorService.updateMetadataForTaxonomyEntities(anyList())).thenAnswer(invocationOnMock -> {
                final var inputObjects = (List<MetadataDto>) invocationOnMock.getArgument(0);

                return inputObjects
                        .stream()
                        .map(inputObject -> {
                            if (inputObject == entity1ToUpdate) {
                                return entity1ToReturn;
                            } else if (inputObject == entity2ToUpdate) {
                                return entity2ToReturn;
                            }

                            return null;
                        })
                        .collect(Collectors.toList());
            });

            final var returnedObjects = metadataController.putBulk(List.of(entity1ToUpdate, entity2ToUpdate).toArray(new MetadataDto[0]), mock(BindingResult.class));

            assertEquals(2, returnedObjects.size());
            assertSame(entity1ToReturn, returnedObjects.get(0));
            assertSame(entity2ToReturn, returnedObjects.get(1));
        }

        {
            final var entityToUpdate = mock(MetadataDto.class);

            when(metadataAggregatorService.updateMetadataForTaxonomyEntities(anyList())).thenThrow(new InvalidPublicIdException(""));

            try {
                metadataController.putBulk(List.of(entityToUpdate).toArray(new MetadataDto[0]), mock(BindingResult.class));
                fail("Expected InvalidRequestException");
            } catch (InvalidRequestException e) {
                assertTrue(e.getCause() instanceof InvalidPublicIdException);
            }
        }
    }
}