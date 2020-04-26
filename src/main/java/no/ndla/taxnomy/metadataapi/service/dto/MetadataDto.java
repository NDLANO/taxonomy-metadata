package no.ndla.taxnomy.metadataapi.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

public class MetadataDto {
    public static class CompetenceAim {
        @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Error validating competence aim, must only contain letters, numbers and -")
        @NotEmpty(message = "Competence aim code is empty")
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public CompetenceAim(String code) {
            this.code = code;
        }

        CompetenceAim() {

        }
    }

    private String publicId;

    private Boolean visible;

    @Valid
    private Set<CompetenceAim> competenceAims;

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public Set<CompetenceAim> getCompetenceAims() {
        return competenceAims;
    }

    public void setCompetenceAims(Set<CompetenceAim> competenceAims) {
        this.competenceAims = Set.copyOf(competenceAims);
    }

    public MetadataDto(String publicId) {
        this.publicId = publicId;
    }

    public MetadataDto() {

    }

    public void populateEmpty() {
        if (this.competenceAims == null) {
            this.competenceAims = new HashSet<>();
        }

        this.visible = true;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
