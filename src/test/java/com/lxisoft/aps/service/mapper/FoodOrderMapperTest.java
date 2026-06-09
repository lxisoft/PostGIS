package com.lxisoft.aps.service.mapper;

import static com.lxisoft.aps.domain.FoodOrderAsserts.*;
import static com.lxisoft.aps.domain.FoodOrderTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FoodOrderMapperTest {

    private FoodOrderMapper foodOrderMapper;

    @BeforeEach
    void setUp() {
        foodOrderMapper = new FoodOrderMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getFoodOrderSample1();
        var actual = foodOrderMapper.toEntity(foodOrderMapper.toDto(expected));
        assertFoodOrderAllPropertiesEquals(expected, actual);
    }
}
