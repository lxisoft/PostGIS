package com.lxisoft.aps.domain;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("customer")
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("name")
    private String name;

    // PostGIS geography(Point, 4326) — stored as WGS-84 GPS coordinate
    @Column("home_location")
    private Point homeLocation;

    public Long getId() {
        return this.id;
    }

    public Customer id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Customer name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getHomeLocation() {
        return homeLocation;
    }

    public Customer homeLocation(Point homeLocation) {
        this.homeLocation = homeLocation;
        return this;
    }

    public void setHomeLocation(Point homeLocation) {
        this.homeLocation = homeLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Customer)) {
            return false;
        }
        return getId() != null && getId().equals(((Customer) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Customer{" + "id=" + getId() + ", name='" + getName() + "'" + "}";
    }
}
