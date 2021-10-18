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
public class TaxonomyEntity {
    @Id private UUID id;

    @Column private String publicId;

    @UpdateTimestamp private Instant updatedAt;
    @CreationTimestamp private Instant createdAt;

    @SuppressWarnings("JpaDataSourceORMInspection")
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "taxonomy_entity_competence_aim",
            joinColumns = @JoinColumn(name = "taxonomy_entity_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_aim_id"))
    private Set<CompetenceAim> competenceAims = new HashSet<>();

    // JPA will delete custom field values automatically if they are removed from this set and this
    // entity persisted.
    // This is for the @PreRemoval to take effect and automatically remove values when this entity
    // is deleted
    // - makes sense - right? otherwise service level code would have to always delete values before
    // deleting this.
    @OneToMany(
            cascade = {
                CascadeType.REMOVE,
                CascadeType.PERSIST,
                CascadeType.MERGE,
                CascadeType.REFRESH
            },
            orphanRemoval = true,
            mappedBy = "taxonomyEntity")
    private Set<CustomFieldValue> customFieldValues;

    @Column private boolean visible = true;

    public void addCompetenceAim(CompetenceAim competenceAim) {
        this.competenceAims.add(competenceAim);

        if (!competenceAim.containsTaxonomyEntity(this)) {
            competenceAim.addTaxonomyEntity(this);
        }
    }

    public void removeCompetenceAim(CompetenceAim competenceAim) {
        this.competenceAims.remove(competenceAim);

        if (competenceAim.containsTaxonomyEntity(this)) {
            competenceAim.removeTaxonomyEntity(this);
        }
    }

    public Set<CompetenceAim> getCompetenceAims() {
        return competenceAims.stream().collect(Collectors.toUnmodifiableSet());
    }

    public UUID getId() {
        return id;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    @PrePersist
    void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @PreRemove
    void preRemove() {
        // De-links the competence aims before removal (but keeps the competence aim entities)
        Set.copyOf(this.competenceAims).forEach(this::removeCompetenceAim);
        if (customFieldValues != null) {
            customFieldValues.clear();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
