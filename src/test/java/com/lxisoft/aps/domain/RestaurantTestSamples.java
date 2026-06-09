package com.lxisoft.aps.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class RestaurantTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Restaurant getRestaurantSample1() {
        return new Restaurant().id(1L).name("name1").cuisine("cuisine1").locationString("locationString1");
    }

    public static Restaurant getRestaurantSample2() {
        return new Restaurant().id(2L).name("name2").cuisine("cuisine2").locationString("locationString2");
    }

    public static Restaurant getRestaurantRandomSampleGenerator() {
        return new Restaurant()
            .id(longCount.incrementAndGet())
            .name(UUID.randomUUID().toString())
            .cuisine(UUID.randomUUID().toString())
            .locationString(UUID.randomUUID().toString());
    }
}
