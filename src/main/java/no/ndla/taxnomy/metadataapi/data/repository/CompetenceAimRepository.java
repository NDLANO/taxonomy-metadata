package no.ndla.taxnomy.metadataapi.data.repository;

import no.ndla.taxnomy.metadataapi.data.domain.CompetenceAim;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetenceAimRepository extends JpaRepository<CompetenceAim, UUID> {
    Optional<CompetenceAim> findFirstByCode(String code);
}
