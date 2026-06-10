package com.lxisoft.aps.domain;

import com.lxisoft.aps.domain.enumeration.PartnerStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A DeliveryPartner.
 */
@Table("delivery_partner")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeliveryPartner implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("name")
    private String name;

    @NotNull(message = "must not be null")
    @Column("status")
    private PartnerStatus status;

    @Column("location")
    private String location;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public DeliveryPartner id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public DeliveryPartner name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PartnerStatus getStatus() {
        return this.status;
    }

    public DeliveryPartner status(PartnerStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(PartnerStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return this.location;
    }

    public DeliveryPartner location(String location) {
        this.setLocation(location);
        return this;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliveryPartner)) {
            return false;
        }
        return getId() != null && getId().equals(((DeliveryPartner) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeliveryPartner{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", status='" + getStatus() + "'" +
            ", location='" + getLocation() + "'" +
            "}";
    }
}
