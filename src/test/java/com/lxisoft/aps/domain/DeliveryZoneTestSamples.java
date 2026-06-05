package com.lxisoft.aps.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DeliveryZoneTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static DeliveryZone getDeliveryZoneSample1() {
        return new DeliveryZone().id(1L).name("name1").description("description1");
    }

    public static DeliveryZone getDeliveryZoneSample2() {
        return new DeliveryZone().id(2L).name("name2").description("description2");
    }

    public static DeliveryZone getDeliveryZoneRandomSampleGenerator() {
        return new DeliveryZone()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString());
    }
}
