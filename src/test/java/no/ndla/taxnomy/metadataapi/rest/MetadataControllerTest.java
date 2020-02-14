package no.ndla.taxnomy.metadataapi.rest;

import no.ndla.taxnomy.metadataapi.rest.exception.InvalidRequestException;
import no.ndla.taxnomy.metadataapi.service.MetadataAggregatorService;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
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
    void put() throws InvalidPublicIdException {
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
            requestObject.addCompetenceAim(aim1);
            requestObject.addCompetenceAim(aim2);

            final var returnedResponseEntity = metadataController.put("urn:test:1", requestObject, bindingResult);

            assertEquals(HttpStatus.OK, returnedResponseEntity.getStatusCode());

            final var returnedMetadataDto = returnedResponseEntity.getBody();
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
}