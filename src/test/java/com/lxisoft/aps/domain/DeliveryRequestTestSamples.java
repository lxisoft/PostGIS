package com.lxisoft.aps.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class DeliveryRequestTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static DeliveryRequest getDeliveryRequestSample1() {
        return new DeliveryRequest().id(1L).status("status1");
    }

    public static DeliveryRequest getDeliveryRequestSample2() {
        return new DeliveryRequest().id(2L).status("status2");
    }

    public static DeliveryRequest getDeliveryRequestRandomSampleGenerator() {
        return new DeliveryRequest().id(longCount.incrementAndGet()).status(UUID.randomUUID().toString());
    }
}
