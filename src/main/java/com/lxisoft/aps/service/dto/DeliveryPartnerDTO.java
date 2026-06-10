package com.lxisoft.aps.service.dto;

import com.lxisoft.aps.domain.enumeration.PartnerStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.lxisoft.aps.domain.DeliveryPartner} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeliveryPartnerDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private String name;

    @NotNull(message = "must not be null")
    private PartnerStatus status;

    private String location;

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

    public PartnerStatus getStatus() {
        return status;
    }

    public void setStatus(PartnerStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliveryPartnerDTO)) {
            return false;
        }

        DeliveryPartnerDTO deliveryPartnerDTO = (DeliveryPartnerDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, deliveryPartnerDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeliveryPartnerDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", status='" + getStatus() + "'" +
            ", location='" + getLocation() + "'" +
            "}";
    }
}
