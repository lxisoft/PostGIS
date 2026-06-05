package com.lxisoft.aps.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.lxisoft.aps.domain.DeliveryRequest} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeliveryRequestDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private Instant requestDate;

    @NotNull(message = "must not be null")
    private String status;

    private CustomerDTO customer;

    private Double deliveryLatitude;

    private Double deliveryLongitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Instant requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    public void setDeliveryLatitude(Double deliveryLatitude) {
        this.deliveryLatitude = deliveryLatitude;
    }

    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    public void setDeliveryLongitude(Double deliveryLongitude) {
        this.deliveryLongitude = deliveryLongitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliveryRequestDTO)) {
            return false;
        }

        DeliveryRequestDTO deliveryRequestDTO = (DeliveryRequestDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, deliveryRequestDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeliveryRequestDTO{" +
            "id=" + getId() +
            ", requestDate='" + getRequestDate() + "'" +
            ", status='" + getStatus() + "'" +
            ", customer=" + getCustomer() +
            "}";
    }
}
