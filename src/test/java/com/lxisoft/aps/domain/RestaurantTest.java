package com.lxisoft.aps.domain;

import static com.lxisoft.aps.domain.RestaurantTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.lxisoft.aps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RestaurantTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Restaurant.class);
        Restaurant restaurant1 = getRestaurantSample1();
        Restaurant restaurant2 = new Restaurant();
        assertThat(restaurant1).isNotEqualTo(restaurant2);

        restaurant2.setId(restaurant1.getId());
        assertThat(restaurant1).isEqualTo(restaurant2);

        restaurant2 = getRestaurantSample2();
        assertThat(restaurant1).isNotEqualTo(restaurant2);
    }
}
