package com.satur9er.jmp.cache.statictics;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicLong;

@Log4j2
public class EvictionCounter {

    private final AtomicLong evictionsCount = new AtomicLong();

    public void count() {
        evictionsCount.incrementAndGet();
    }

    public long getCount() {
        return evictionsCount.get();
    }

}
