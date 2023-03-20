package com.satur9er.spurt.cache.impl;

import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

public class SimpleCacheServiceTest extends CacheServiceTest {

    @BeforeEach
    void setUp() {
        uut = new SimpleCacheService<>(CAPACITY, EVICTION_DELAY, TimeUnit.MILLISECONDS, (k, v) -> {});
    }

}
