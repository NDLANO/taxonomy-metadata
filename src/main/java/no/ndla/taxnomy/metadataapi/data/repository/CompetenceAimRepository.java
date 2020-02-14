package no.ndla.taxnomy.metadataapi.data.repository;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompetenceAimRepository extends JpaRepository<CompetenceAim, UUID> {
    Optional<CompetenceAim> findFirstByCode(String code);
}
