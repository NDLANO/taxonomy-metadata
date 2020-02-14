package no.ndla.taxnomy.metadataapi.data.repository;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaxonomyEntityRepository extends JpaRepository<TaxonomyEntity, UUID> {
    Optional<TaxonomyEntity> findFirstByPublicId(String publicId);
}
