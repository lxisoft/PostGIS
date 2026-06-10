package com.lxisoft.aps.service.mapper;

import static com.lxisoft.aps.domain.RestaurantAsserts.*;
import static com.lxisoft.aps.domain.RestaurantTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RestaurantMapperTest {

    private RestaurantMapper restaurantMapper;

    @BeforeEach
    void setUp() {
        restaurantMapper = new RestaurantMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getRestaurantSample1();
        var actual = restaurantMapper.toEntity(restaurantMapper.toDto(expected));
        assertRestaurantAllPropertiesEquals(expected, actual);
    }
}
