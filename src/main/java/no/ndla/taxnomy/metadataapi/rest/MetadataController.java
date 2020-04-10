package no.ndla.taxnomy.metadataapi.rest;

import no.ndla.taxnomy.metadataapi.rest.exception.InvalidRequestException;
import no.ndla.taxnomy.metadataapi.service.MetadataAggregatorService;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/taxonomy_entities/{publicId}")
public class MetadataController {
    private final MetadataAggregatorService metadataAggregatorService;

    public MetadataController(MetadataAggregatorService metadataAggregatorService) {
        this.metadataAggregatorService = metadataAggregatorService;
    }

    @GetMapping
    public MetadataDto get(@PathVariable String publicId) {
        try {
            return metadataAggregatorService.getMetadataForTaxonomyEntity(publicId);
        } catch (InvalidPublicIdException e) {
            throw new InvalidRequestException(e);
        }
    }

    @PutMapping
    public ResponseEntity<MetadataDto> put(@PathVariable String publicId, @RequestBody @Valid MetadataDto requestMetadataDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult.getErrorCount() + " errors in provided body, first error: " + bindingResult.getAllErrors().get(0));
        }

        try {
            final var metadataDto = metadataAggregatorService.updateMetadataForTaxonomyEntity(publicId, requestMetadataDto);

            return ResponseEntity
                    .ok()
                    .body(metadataDto);
        } catch (InvalidPublicIdException e) {
            throw new InvalidRequestException(e);
        }
    }

    @DeleteMapping
    public ResponseEntity<String> delete(@PathVariable String publicId) {
        try {
            metadataAggregatorService.deleteMetadataForTaxonomyEntity(publicId);

            return ResponseEntity.noContent().build();
        } catch (InvalidPublicIdException e) {
            throw new InvalidRequestException(e);
        }
    }
}
