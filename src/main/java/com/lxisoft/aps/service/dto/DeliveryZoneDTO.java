package com.lxisoft.aps.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.lxisoft.aps.domain.DeliveryZone} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeliveryZoneDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private String name;

    private String description;

    @NotNull(message = "must not be null")
    private Boolean active;

    /** Zone boundary polygon serialized as GeoJSON string for frontend map rendering */
    private String boundaryGeoJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getBoundaryGeoJson() {
        return boundaryGeoJson;
    }

    public void setBoundaryGeoJson(String boundaryGeoJson) {
        this.boundaryGeoJson = boundaryGeoJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliveryZoneDTO)) {
            return false;
        }

        DeliveryZoneDTO deliveryZoneDTO = (DeliveryZoneDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, deliveryZoneDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeliveryZoneDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", description='" + getDescription() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
