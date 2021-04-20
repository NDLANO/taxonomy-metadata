package no.ndla.taxnomy.metadataapi.rest;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import no.ndla.taxnomy.metadataapi.rest.exception.InvalidRequestException;
import no.ndla.taxnomy.metadataapi.service.CustomFieldService;
import no.ndla.taxnomy.metadataapi.service.MetadataAggregatorService;
import no.ndla.taxnomy.metadataapi.service.dto.MetadataDto;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidDataException;
import no.ndla.taxnomy.metadataapi.service.exception.InvalidPublicIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/taxonomy_entities")
public class MetadataController {
    private final Logger logger = LoggerFactory.getLogger(MetadataController.class);
    private final MetadataAggregatorService metadataAggregatorService;
    private final CustomFieldService customFieldService;

    public MetadataController(MetadataAggregatorService metadataAggregatorService, CustomFieldService customFieldService) {
        this.metadataAggregatorService = metadataAggregatorService;
        this.customFieldService = customFieldService;
    }

    private Instant efficiencyWarnRatelimit = null;
    @GetMapping
    public List<MetadataDto> getMultiple(@RequestParam(required = false) String publicIds, @RequestParam(required = false) String key, @RequestParam(required = false) String value) {
        // Read comma separated list of unique publicIds in query parameter

        final Set<String> publicIdSet;
        if (publicIds == null) {
            if (key == null || value == null) {
                throw new InvalidRequestException("Query publicIds and key/value not specified");
            }
            publicIdSet = customFieldService.getTaxonomyEntitiesByCustomFieldKeyValue(key, value).stream()
                    .map(TaxonomyEntity::getPublicId)
                    .collect(Collectors.toSet());

            if (publicIdSet.size() > 100) {
                /*
                 * Ratelimited logging, max 1 per 10min per head.
                 */
                final boolean printWarn;
                if (efficiencyWarnRatelimit == null) {
                    synchronized (this) {
                        if (efficiencyWarnRatelimit == null) {
                            efficiencyWarnRatelimit = Instant.now();
                            printWarn = true;
                        } else {
                            printWarn = false;
                        }
                    }
                } else {
                    Instant efficiencyWarnRatelimit = this.efficiencyWarnRatelimit;
                    Instant now = Instant.now();
                    if (!Duration.between(efficiencyWarnRatelimit, now).minus(Duration.ofMinutes(10)).isNegative()) {
                        synchronized (this) {
                            if (efficiencyWarnRatelimit == this.efficiencyWarnRatelimit) {
                                this.efficiencyWarnRatelimit = now;
                                printWarn = true;
                            } else {
                                printWarn = false;
                            }
                        }
                    } else {
                        printWarn = false;
                    }
                }
                if (printWarn) {
                    logger.warn("Query for key/value had more than 100 results. This is starting to become inefficient/slow. (Refactor?)");
                }
            }
        } else if (publicIds.length() == 0) {
            return List.of();
        } else if (key != null || value != null) {
            if (key == null || value == null) {
                throw new InvalidRequestException("Query publicIds and none or both key/value not specified");
            }
            final var publicIdFilter = new HashSet<>(Arrays.asList(publicIds.split(",")));
            publicIdSet = customFieldService.getTaxonomyEntitiesByCustomFieldKeyValue(key, value).stream()
                    .map(TaxonomyEntity::getPublicId)
                    .filter(publicIdFilter::contains)
                    .collect(Collectors.toSet());

            if (publicIdSet.size() > 100) {
                throw new InvalidRequestException("Cannot get metadata for more than 100 entities in each request");
            }
        } else {
            publicIdSet = new HashSet<>(Arrays.asList(publicIds.split(",")));

            if (publicIdSet.size() > 100) {
                throw new InvalidRequestException("Cannot get metadata for more than 100 entities in each request");
            }
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
    public MetadataDto put(@PathVariable String publicId, @RequestBody @Valid MetadataDto requestMetadataDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult.getErrorCount() + " errors in provided body, first error: " + bindingResult.getAllErrors().get(0));
        }

        try {
            return metadataAggregatorService.updateMetadataForTaxonomyEntity(publicId, requestMetadataDto);
        } catch (InvalidDataException | InvalidPublicIdException e) {
            throw new InvalidRequestException(e);
        }
    }

    @PutMapping("/")
    public List<MetadataDto> putBulk(@RequestBody @Valid MetadataDto[] requestMetadataDtos, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new InvalidRequestException(bindingResult.getErrorCount() + " errors in provided body, first error: " + bindingResult.getAllErrors().get(0));
        }

        try {
            return metadataAggregatorService.updateMetadataForTaxonomyEntities(Arrays.asList(requestMetadataDtos));
        } catch (InvalidDataException | InvalidPublicIdException e) {
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
