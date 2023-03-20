package com.satur9er.spurt.cache.statictics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunningAverageDurationCounterTest {

    private RunningAverageDurationCounter uut;

    @BeforeEach
    void setUp() {
        uut = new RunningAverageDurationCounter();
    }

    @DisplayName("Should calculate running average")
    @CsvSource({"10,10", "15,10+20", "20,10+20+30", "17,10+20+30+10"})
    @ParameterizedTest(name = "Average of {1} is {0}")
    void testIncreaseAndGet(long expected, String incrementsString) {
        long[] increments = Arrays.stream(incrementsString.split("\\+")).mapToLong(Long::valueOf).toArray();

        for (long increment : increments) {
            uut.addNewDuration(0, increment);
        }

        assertEquals(expected, uut.getAverageDuration());
    }

}