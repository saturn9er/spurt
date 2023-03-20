package com.satur9er.jmp.cache.impl.guava;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.satur9er.jmp.cache.api.JmpCacheService;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@Log4j2
public class GuavaJmpCacheService<K, V> implements JmpCacheService<K, V> {

    private final Cache<K, V> cache;

    public GuavaJmpCacheService(int capacity,
                                long evictionDelay,
                                TimeUnit evictionDelayUnit,
                                BiConsumer<K, V> evictionListener) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(capacity)
                .expireAfterAccess(evictionDelay, evictionDelayUnit)
                .removalListener(notification -> evictionListener.accept((K) notification.getKey(), (V) notification.getValue()))
                .recordStats()
                .build();
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(K key, V value) {
        try {
            cache.get(key, () -> value);
            log.debug("Key-Value added: {}:{}", key, value);
        } catch (ExecutionException e) {
            log.warn("Failed to put Key-Value pair: {}:{} with exception", key, value, e);
        }

        printAvgPutDuration();
        printTotalEvictionCount();
    }

    private void printAvgPutDuration() {
        if (cache.stats().loadSuccessCount() == 0) return;
        long avgPutTime = cache.stats().totalLoadTime() / cache.stats().loadCount();
        log.debug("Average time spent putting new values into the cache: {} ns", avgPutTime);
    }

    private void printTotalEvictionCount() {
        log.debug("Total eviction count: {}", cache.stats().evictionCount());
    }

}
