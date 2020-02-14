package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;

public interface CompetenceAimService {
    CompetenceAim getOrCreateCompetenceAim(String code);
}
