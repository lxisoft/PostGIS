package com.lxisoft.aps.domain;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.locationtech.jts.geom.Polygon;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("delivery_zone")
public class DeliveryZone implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @NotNull(message = "must not be null")
    @Column("active")
    private Boolean active;

    // PostGIS geography(Polygon, 4326) — service area boundary stored as WGS-84 polygon
    @Column("boundary")
    private Polygon boundary;

    public Long getId() {
        return this.id;
    }

    public DeliveryZone id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public DeliveryZone name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public DeliveryZone description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return this.active;
    }

    public DeliveryZone active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Polygon getBoundary() {
        return boundary;
    }

    public DeliveryZone boundary(Polygon boundary) {
        this.boundary = boundary;
        return this;
    }

    public void setBoundary(Polygon boundary) {
        this.boundary = boundary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliveryZone)) {
            return false;
        }
        return getId() != null && getId().equals(((DeliveryZone) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "DeliveryZone{" +
            "id=" +
            getId() +
            ", name='" +
            getName() +
            "'" +
            ", description='" +
            getDescription() +
            "'" +
            ", active='" +
            getActive() +
            "'" +
            "}"
        );
    }
}
