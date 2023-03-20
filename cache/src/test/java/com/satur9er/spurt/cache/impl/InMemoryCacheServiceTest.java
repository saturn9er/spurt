package com.satur9er.spurt.cache.impl;

import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

public class InMemoryCacheServiceTest extends CacheServiceTest {

    @BeforeEach
    void setUp() {
        uut = new InMemoryCacheService<>(CAPACITY, EVICTION_DELAY, TimeUnit.MILLISECONDS, (k, v) -> {});
    }

}
