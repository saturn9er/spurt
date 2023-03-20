package com.satur9er.spurt.cache.impl.guava;

import com.satur9er.spurt.cache.impl.CacheServiceTest;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

public class GuavaCacheServiceTest extends CacheServiceTest {

    @BeforeEach
    void setUp() {
        uut = new GuavaCacheService<>(CAPACITY, EVICTION_DELAY, TimeUnit.MILLISECONDS, (k, v) -> {});
    }

}
