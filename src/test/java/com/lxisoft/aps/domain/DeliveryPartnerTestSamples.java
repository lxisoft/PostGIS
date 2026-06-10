package com.lxisoft.aps.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DeliveryPartnerTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static DeliveryPartner getDeliveryPartnerSample1() {
        return new DeliveryPartner().id(1L).name("name1").location("location1");
    }

    public static DeliveryPartner getDeliveryPartnerSample2() {
        return new DeliveryPartner().id(2L).name("name2").location("location2");
    }

    public static DeliveryPartner getDeliveryPartnerRandomSampleGenerator() {
        return new DeliveryPartner()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .location(UUID.randomUUID().toString());
    }
}
