package no.ndla.taxnomy.metadataapi.data.repository;

import no.ndla.taxnomy.metadataapi.data.domain.CustomField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomFieldRepository extends JpaRepository<CustomField, UUID> {
    Optional<CustomField> findFirstByKey(String key);

    default Optional<CustomField> findByKey(String key) {
        return findFirstByKey(key);
    }
}
