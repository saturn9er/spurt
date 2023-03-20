package com.satur9er.jmp.cache.statictics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvictionCounterTest {

    private EvictionCounter uut;

    @BeforeEach
    void setUp() {
        uut = new EvictionCounter();
    }

    @DisplayName("Should count up")
    @Test
    void testCount() {
        uut.count();

        assertEquals(1, uut.getCount());
    }

}