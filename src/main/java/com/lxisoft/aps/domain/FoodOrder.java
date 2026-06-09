package com.lxisoft.aps.domain;

import com.lxisoft.aps.domain.enumeration.OrderStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A FoodOrder.
 */
@Table("food_order")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FoodOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull(message = "must not be null")
    @Column("customer_name")
    private String customerName;

    @NotNull(message = "must not be null")
    @Column("delivery_address")
    private String deliveryAddress;

    @Column("delivery_location")
    private String deliveryLocation;

    @NotNull(message = "must not be null")
    @Column("status")
    private OrderStatus status;

    @org.springframework.data.annotation.Transient
    private Restaurant restaurant;

    @org.springframework.data.annotation.Transient
    private DeliveryPartner deliveryPartner;

    @Column("restaurant_id")
    private Long restaurantId;

    @Column("delivery_partner_id")
    private Long deliveryPartnerId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public FoodOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public FoodOrder customerName(String customerName) {
        this.setCustomerName(customerName);
        return this;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDeliveryAddress() {
        return this.deliveryAddress;
    }

    public FoodOrder deliveryAddress(String deliveryAddress) {
        this.setDeliveryAddress(deliveryAddress);
        return this;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryLocation() {
        return this.deliveryLocation;
    }

    public FoodOrder deliveryLocation(String deliveryLocation) {
        this.setDeliveryLocation(deliveryLocation);
        return this;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public FoodOrder status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Restaurant getRestaurant() {
        return this.restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.restaurantId = restaurant != null ? restaurant.getId() : null;
    }

    public FoodOrder restaurant(Restaurant restaurant) {
        this.setRestaurant(restaurant);
        return this;
    }

    public DeliveryPartner getDeliveryPartner() {
        return this.deliveryPartner;
    }

    public void setDeliveryPartner(DeliveryPartner deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
        this.deliveryPartnerId = deliveryPartner != null ? deliveryPartner.getId() : null;
    }

    public FoodOrder deliveryPartner(DeliveryPartner deliveryPartner) {
        this.setDeliveryPartner(deliveryPartner);
        return this;
    }

    public Long getRestaurantId() {
        return this.restaurantId;
    }

    public void setRestaurantId(Long restaurant) {
        this.restaurantId = restaurant;
    }

    public Long getDeliveryPartnerId() {
        return this.deliveryPartnerId;
    }

    public void setDeliveryPartnerId(Long deliveryPartner) {
        this.deliveryPartnerId = deliveryPartner;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FoodOrder)) {
            return false;
        }
        return getId() != null && getId().equals(((FoodOrder) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FoodOrder{" +
            "id=" + getId() +
            ", customerName='" + getCustomerName() + "'" +
            ", deliveryAddress='" + getDeliveryAddress() + "'" +
            ", deliveryLocation='" + getDeliveryLocation() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
