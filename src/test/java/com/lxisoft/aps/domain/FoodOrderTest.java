package com.lxisoft.aps.domain;

import static com.lxisoft.aps.domain.DeliveryPartnerTestSamples.*;
import static com.lxisoft.aps.domain.FoodOrderTestSamples.*;
import static com.lxisoft.aps.domain.RestaurantTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FoodOrderTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FoodOrder.class);
        FoodOrder foodOrder1 = getFoodOrderSample1();
        FoodOrder foodOrder2 = new FoodOrder();
        assertThat(foodOrder1).isNotEqualTo(foodOrder2);

        foodOrder2.setId(foodOrder1.getId());
        assertThat(foodOrder1).isEqualTo(foodOrder2);

        foodOrder2 = getFoodOrderSample2();
        assertThat(foodOrder1).isNotEqualTo(foodOrder2);
    }

    @Test
    void restaurantTest() {
        FoodOrder foodOrder = getFoodOrderRandomSampleGenerator();
        Restaurant restaurantBack = getRestaurantRandomSampleGenerator();

        foodOrder.setRestaurant(restaurantBack);
        assertThat(foodOrder.getRestaurant()).isEqualTo(restaurantBack);

        foodOrder.restaurant(null);
        assertThat(foodOrder.getRestaurant()).isNull();
    }

    @Test
    void deliveryPartnerTest() {
        FoodOrder foodOrder = getFoodOrderRandomSampleGenerator();
        DeliveryPartner deliveryPartnerBack = getDeliveryPartnerRandomSampleGenerator();

        foodOrder.setDeliveryPartner(deliveryPartnerBack);
        assertThat(foodOrder.getDeliveryPartner()).isEqualTo(deliveryPartnerBack);

        foodOrder.deliveryPartner(null);
        assertThat(foodOrder.getDeliveryPartner()).isNull();
    }
}
