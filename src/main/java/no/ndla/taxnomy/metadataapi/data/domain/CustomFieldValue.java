package no.ndla.taxnomy.metadataapi.data.domain;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class CustomFieldValue {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "taxonomy_entity_id")
    private TaxonomyEntity taxonomyEntity;

    @ManyToOne
    @JoinColumn(name = "custom_field_id")
    private CustomField customField;

    @Column
    private String value;

    @PrePersist
    void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TaxonomyEntity getTaxonomyEntity() {
        return taxonomyEntity;
    }

    public void setTaxonomyEntity(TaxonomyEntity taxonomyEntity) {
        this.taxonomyEntity = taxonomyEntity;
    }

    public CustomField getCustomField() {
        return customField;
    }

    public void setCustomField(CustomField customField) {
        this.customField = customField;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
