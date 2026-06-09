package com.lxisoft.aps.service.mapper;

import static com.lxisoft.aps.domain.DeliveryPartnerAsserts.*;
import static com.lxisoft.aps.domain.DeliveryPartnerTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeliveryPartnerMapperTest {

    private DeliveryPartnerMapper deliveryPartnerMapper;

    @BeforeEach
    void setUp() {
        deliveryPartnerMapper = new DeliveryPartnerMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDeliveryPartnerSample1();
        var actual = deliveryPartnerMapper.toEntity(deliveryPartnerMapper.toDto(expected));
        assertDeliveryPartnerAllPropertiesEquals(expected, actual);
    }
}
