package com.lxisoft.aps.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FoodOrderTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static FoodOrder getFoodOrderSample1() {
        return new FoodOrder()
            .id(1L)
            .customerName("customerName1")
            .deliveryAddress("deliveryAddress1")
            .deliveryLocation("deliveryLocation1");
    }

    public static FoodOrder getFoodOrderSample2() {
        return new FoodOrder()
            .id(2L)
            .customerName("customerName2")
            .deliveryAddress("deliveryAddress2")
            .deliveryLocation("deliveryLocation2");
    }

    public static FoodOrder getFoodOrderRandomSampleGenerator() {
        return new FoodOrder()
            .id(longCount.incrementAndGet())
            .customerName(UUID.randomUUID().toString())
            .deliveryAddress(UUID.randomUUID().toString())
            .deliveryLocation(UUID.randomUUID().toString());
    }
}
