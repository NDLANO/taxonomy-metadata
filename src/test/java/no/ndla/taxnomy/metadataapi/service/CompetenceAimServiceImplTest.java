package no.ndla.taxnomy.metadataapi.service;

import no.ndla.taxnomy.metadataapi.data.repository.CompetenceAimRepository;
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
class CompetenceAimServiceImplTest {
    private CompetenceAimRepository competenceAimRepository;
    private CompetenceAimServiceImpl competenceAimService;

    @BeforeEach
    void setUp(@Autowired CompetenceAimRepository competenceAimRepository) {
        this.competenceAimRepository = competenceAimRepository;
        competenceAimService = new CompetenceAimServiceImpl(competenceAimRepository);
    }

    @Test
    @Transactional
    void getOrCreateCompetenceAim() {
        assertFalse(competenceAimRepository.findFirstByCode("TEST12").isPresent());
        final var aim1 = competenceAimService.getOrCreateCompetenceAim("TEST12");

        assertNotNull(aim1);
        assertNotNull(aim1.getId());

        assertSame(aim1, competenceAimRepository.findFirstByCode("TEST12").orElseThrow());
        assertSame(aim1, competenceAimService.getOrCreateCompetenceAim("TEST12"));
    }
}