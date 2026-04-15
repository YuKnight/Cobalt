package com.github.auties00.cobalt.util;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomIdUtils {
    private final String prefix;
    private final AtomicLong counter;

    public RandomIdUtils() {
        var num1 = FastDataUtils.randomInt(65536);
        var num2 = FastDataUtils.randomInt(65536);
        this.prefix = num1 + "." + num2 + "-";
        this.counter = new AtomicLong(1);
    }

    public String generateId() {
        return prefix + counter.getAndIncrement();
    }

    public static String newId() {
        var num1 = FastDataUtils.randomInt(65536);
        var num2 = FastDataUtils.randomInt(65536);
        return num1 + "." + num2 + "-1";
    }

    public static String generateSid() {
        return Instant.now().getEpochSecond()
               + "-" + FastDataUtils.randomLong(1_000_000_000, 9_999_999_999L)
               + "-" + FastDataUtils.randomInt(0, 1000);
    }
}
