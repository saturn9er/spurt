package com.satur9er.jmp.cache.statictics;

import java.util.concurrent.atomic.AtomicLong;

public class RunningAverageDurationCounter {

    private final AtomicLong currentAverage;

    private final AtomicLong numberOfElements;

    public RunningAverageDurationCounter() {
        currentAverage = new AtomicLong();
        numberOfElements = new AtomicLong();
    }

    public void addNewDuration(long start, long finish) {
        currentAverage.getAndUpdate(currentAvg -> {
            long newDeltaNanos = finish - start;
            return (numberOfElements.get() * currentAvg + newDeltaNanos) / numberOfElements.incrementAndGet();
        });
    }

    public long getAverageDuration() {
        return currentAverage.get();
    }

}
