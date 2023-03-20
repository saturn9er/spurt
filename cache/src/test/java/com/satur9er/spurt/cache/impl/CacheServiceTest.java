package com.satur9er.jmp.cache.impl;

import com.satur9er.jmp.cache.api.JmpCacheService;
import com.satur9er.jmp.cache.impl.SimpleJmpCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class JmpCacheServiceTest {

    protected static final int CAPACITY = 3;
    protected static final long EVICTION_DELAY = 100;

    protected static final int WAIT_COEFFICIENT = 3;

    protected JmpCacheService<String, String> uut;

    @BeforeEach
    void setUp() {
        uut = new SimpleJmpCacheService<>(CAPACITY, EVICTION_DELAY, TimeUnit.MILLISECONDS, (k, v) -> {});
        //uut = new GuavaJmpCacheService<>(CAPACITY, EVICTION_DELAY, TimeUnit.MILLISECONDS, (k, v) -> {});
    }

    @DisplayName("Should be able to store entries")
    @Test
    void testPutAndGet() {
        uut.put("key1", "value1");
        uut.put("key2", "value2");
        uut.put("key3", "value3");

        assertEquals("value1", uut.get("key1"));
        assertEquals("value2", uut.get("key2"));
        assertEquals("value3", uut.get("key3"));
    }

    @DisplayName("Should perform action on removal/eviction")
    @Nested
    class RemovalListenerTest {

        private List<String> mutableList;

        @BeforeEach
        void setUp() {
            mutableList = new ArrayList<>();
            uut = new SimpleJmpCacheService<>(
                    1,
                    1, TimeUnit.MILLISECONDS,
                    (k, v) -> mutableList.add(k)
            );
        }

        @DisplayName("After time-based eviction")
        @Test
        void testRemovalListenerAfterTime() throws InterruptedException {
            // GIVEN
            var evictedKey = "my-key-1";

            // WHEN
            uut.put(evictedKey, "value");

            // THEN
            waitForMs(10);
            assertEquals(1, mutableList.size());
            assertEquals(evictedKey, mutableList.get(0));
        }

        @DisplayName("After capacity eviction")
        @Test
        void testRemovalListenerAfterCapacity() {
            // GIVEN
            var evictedKey = "my-key-1";

            // WHEN
            uut.put(evictedKey, "value");
            uut.put("key", "value");

            // THEN
            assertEquals(1, mutableList.size());
            assertEquals(evictedKey, mutableList.get(0));
        }

    }

    @DisplayName("Should evict entries")
    @Nested
    class EvictionTest {

        @DisplayName("After some set time passes")
        @Test
        void testEvictionByTime() throws InterruptedException {
            uut.put("key1", "value1");
            uut.put("key2", "value2");
            uut.put("key3", "value3");

            waitForMs(EVICTION_DELAY);

            assertNull(uut.get("key1"));
            assertNull(uut.get("key2"));
            assertNull(uut.get("key3"));
        }

        @DisplayName("After capacity is exceed")
        @Test
        void testEvictionByCapacity() {
            uut.put("key1", "value1");
            uut.put("key2", "value2");
            uut.put("key3", "value3");
            uut.put("key4", "value4");

            assertNull(uut.get("key1"));
            assertEquals("value2", uut.get("key2"));
            assertEquals("value3", uut.get("key3"));
            assertEquals("value4", uut.get("key4"));
        }
    }

    private void waitForMs(long ms) throws InterruptedException {
        Thread.sleep(ms * WAIT_COEFFICIENT);
    }

}