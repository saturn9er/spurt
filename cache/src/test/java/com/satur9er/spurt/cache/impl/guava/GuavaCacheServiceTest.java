package com.satur9er.jmp.cache.impl.guava;

import com.satur9er.jmp.cache.impl.JmpCacheServiceTest;
import com.satur9er.jmp.cache.impl.guava.GuavaJmpCacheService;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

public class GuavaJmpCacheServiceTest extends JmpCacheServiceTest {

    @BeforeEach
    void setUp() {
        uut = new GuavaJmpCacheService<>(CAPACITY, EVICTION_DELAY, TimeUnit.MILLISECONDS, (k, v) -> {});
    }

}
