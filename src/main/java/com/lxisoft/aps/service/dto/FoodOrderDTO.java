package com.lxisoft.aps.service.dto;

import com.lxisoft.aps.domain.enumeration.OrderStatus;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.lxisoft.aps.domain.FoodOrder} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class FoodOrderDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private String customerName;

    @NotNull(message = "must not be null")
    private String deliveryAddress;

    private String deliveryLocation;

    @NotNull(message = "must not be null")
    private OrderStatus status;

    private RestaurantDTO restaurant;

    private DeliveryPartnerDTO deliveryPartner;

    // Flat FK fields for easy JSON consumption by the frontend
    private Long restaurantId;

    private Long deliveryPartnerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public RestaurantDTO getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantDTO restaurant) {
        this.restaurant = restaurant;
    }

    public DeliveryPartnerDTO getDeliveryPartner() {
        return deliveryPartner;
    }

    public void setDeliveryPartner(DeliveryPartnerDTO deliveryPartner) {
        this.deliveryPartner = deliveryPartner;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(Long restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Long getDeliveryPartnerId() {
        return deliveryPartnerId;
    }

    public void setDeliveryPartnerId(Long deliveryPartnerId) {
        this.deliveryPartnerId = deliveryPartnerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FoodOrderDTO)) {
            return false;
        }

        FoodOrderDTO foodOrderDTO = (FoodOrderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, foodOrderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "FoodOrderDTO{" +
            "id=" + getId() +
            ", customerName='" + getCustomerName() + "'" +
            ", deliveryAddress='" + getDeliveryAddress() + "'" +
            ", deliveryLocation='" + getDeliveryLocation() + "'" +
            ", status='" + getStatus() + "'" +
            ", restaurant=" + getRestaurant() +
            ", deliveryPartner=" + getDeliveryPartner() +
            "}";
    }
}
