package com.lxisoft.aps.domain;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("delivery_request")
public class DeliveryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("request_date")
    private Instant requestDate;

    @NotNull(message = "must not be null")
    @Column("status")
    private String status;

    @org.springframework.data.annotation.Transient
    private Customer customer;

    @Column("customer_id")
    private Long customerId;

    // PostGIS geography(Point, 4326) — drop-off GPS coordinate for this delivery
    @Column("delivery_location")
    private Point deliveryLocation;

    public Long getId() {
        return this.id;
    }

    public DeliveryRequest id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRequestDate() {
        return this.requestDate;
    }

    public DeliveryRequest requestDate(Instant requestDate) {
        this.setRequestDate(requestDate);
        return this;
    }

    public void setRequestDate(Instant requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return this.status;
    }

    public DeliveryRequest status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        this.customerId = customer != null ? customer.getId() : null;
    }

    public DeliveryRequest customer(Customer customer) {
        this.setCustomer(customer);
        return this;
    }

    public Long getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(Long customer) {
        this.customerId = customer;
    }

    public Point getDeliveryLocation() {
        return deliveryLocation;
    }

    public DeliveryRequest deliveryLocation(Point deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
        return this;
    }

    public void setDeliveryLocation(Point deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliveryRequest)) {
            return false;
        }
        return getId() != null && getId().equals(((DeliveryRequest) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DeliveryRequest{" + "id=" + getId() + ", requestDate='" + getRequestDate() + "'" + ", status='" + getStatus() + "'" + "}";
    }
}
