package no.ndla.taxnomy.metadataapi.data.domain;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
public class CompetenceAim {
    @Id @Column private UUID id;

    @Column private String code;

    @UpdateTimestamp private Instant updatedAt;
    @CreationTimestamp private Instant createdAt;

    @ManyToMany(mappedBy = "competenceAims")
    private Set<TaxonomyEntity> taxonomyEntities = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    void addTaxonomyEntity(TaxonomyEntity taxonomyNode) {
        this.taxonomyEntities.add(taxonomyNode);
    }

    void removeTaxonomyEntity(TaxonomyEntity taxonomyNode) {
        this.taxonomyEntities.remove(taxonomyNode);
    }

    public Set<TaxonomyEntity> getTaxonomyEntities() {
        return taxonomyEntities.stream().collect(Collectors.toUnmodifiableSet());
    }

    boolean containsTaxonomyEntity(TaxonomyEntity taxonomyEntity) {
        return this.taxonomyEntities.contains(taxonomyEntity);
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }
}
