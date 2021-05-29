package no.ndla.taxnomy.metadataapi.data.repository;

import no.ndla.taxnomy.metadataapi.data.domain.CustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, UUID> {
    @Query("SELECT obj FROM CustomFieldValue obj WHERE obj.taxonomyEntity.id = :taxonomyEntity AND obj.customField.id = :customField")
    Optional<CustomFieldValue> findByTaxonomyEntityAndCustomField(UUID taxonomyEntity, UUID customField);

    @Query("SELECT obj FROM CustomFieldValue obj WHERE obj.taxonomyEntity.id = :taxonomyEntity")
    Iterable<CustomFieldValue> findAllByTaxonomyEntity(UUID taxonomyEntity);

    @Query("SELECT obj FROM CustomFieldValue obj LEFT JOIN FETCH obj.taxonomyEntity WHERE obj.customField.id = :customField AND obj.value = :value")
    Iterable<CustomFieldValue> findAllByCustomFieldAndValue(UUID customField, String value);
}
