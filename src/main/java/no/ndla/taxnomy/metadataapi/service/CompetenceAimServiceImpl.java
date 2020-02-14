package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import no.ndla.taxnomy.metadataapi.data.repository.CompetenceAimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;

@Service
public class CompetenceAimServiceImpl implements CompetenceAimService {
    private final CompetenceAimRepository competenceAimRepository;

    public CompetenceAimServiceImpl(CompetenceAimRepository competenceAimRepository) {
        this.competenceAimRepository = competenceAimRepository;
    }

    @Override
    @Transactional(propagation = MANDATORY)
    public CompetenceAim getOrCreateCompetenceAim(String code) {
        return competenceAimRepository.findFirstByCode(code).orElseGet(() -> {
            final var competenceAim = new CompetenceAim();
            competenceAim.setCode(code);

            return competenceAimRepository.saveAndFlush(competenceAim);
        });
    }
}
