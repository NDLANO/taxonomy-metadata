package no.ndla.taxnomy.metadataapi.rest;

import no.ndla.taxnomy.metadataapi.rest.exception.InvalidRequestException;
import no.ndla.taxnomy.metadataapi.service.MetadataAggregatorService;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/v1/taxonomy_entities")
public class MetadataController {
    private final MetadataAggregatorService metadataAggregatorService;

    public MetadataController(MetadataAggregatorService metadataAggregatorService) {
        this.metadataAggregatorService = metadataAggregatorService;
    }

    @GetMapping
    public List<MetadataDto> getMultiple(@RequestParam String publicIds) {
        // Read comma separated list of unique publicIds in query parameter

        if (publicIds.length() == 0) {
            return List.of();
        }

        final var publicIdSet = new HashSet<>(Arrays.asList(publicIds.split(",")));

        if (publicIdSet.size() > 100) {
            throw new InvalidRequestException("Cannot get metadata for more than 100 entities in each request");
        }

        try {
            return metadataAggregatorService.getMetadataForTaxonomyEntities(publicIdSet);
        } catch (InvalidPublicIdException e) {
            throw new InvalidRequestException(e);
        }
    }

    @GetMapping("/{publicId}")
    public MetadataDto get(@PathVariable String publicId) {
        try {
            return metadataAggregatorService.getMetadataForTaxonomyEntity(publicId);
        } catch (InvalidPublicIdException e) {
            throw new InvalidRequestException(e);
        }
    }

    @PutMapping("/{publicId}")
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

    @DeleteMapping("/{publicId}")
    public ResponseEntity<String> delete(@PathVariable String publicId) {
        try {
            metadataAggregatorService.deleteMetadataForTaxonomyEntity(publicId);

            return ResponseEntity.noContent().build();
        } catch (InvalidPublicIdException e) {
            throw new InvalidRequestException(e);
        }
    }
}
