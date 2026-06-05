package com.lxisoft.aps.service.mapper;

import static com.lxisoft.aps.domain.DeliveryRequestAsserts.*;
import static com.lxisoft.aps.domain.DeliveryRequestTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeliveryRequestMapperTest {

    private DeliveryRequestMapper deliveryRequestMapper;

    @BeforeEach
    void setUp() {
        deliveryRequestMapper = new DeliveryRequestMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDeliveryRequestSample1();
        var actual = deliveryRequestMapper.toEntity(deliveryRequestMapper.toDto(expected));
        assertDeliveryRequestAllPropertiesEquals(expected, actual);
    }
}
