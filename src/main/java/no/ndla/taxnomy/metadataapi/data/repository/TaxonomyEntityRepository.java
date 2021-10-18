package no.ndla.taxnomy.metadataapi.data.repository;

import no.ndla.taxnomy.metadataapi.data.domain.TaxonomyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxonomyEntityRepository extends JpaRepository<TaxonomyEntity, UUID> {
    Optional<TaxonomyEntity> findFirstByPublicId(String publicId);

    @Query(
            "SELECT DISTINCT te FROM TaxonomyEntity te LEFT JOIN FETCH te.competenceAims WHERE te.publicId IN :publicIds")
    List<TaxonomyEntity> findAllByPublicIdInIncludingCompetenceAims(Collection<String> publicIds);
}
