package com.satur9er.spurt.cache.impl;

import com.satur9er.spurt.cache.api.CacheService;
import com.satur9er.spurt.cache.statictics.EvictionCounter;
import com.satur9er.spurt.cache.statictics.RunningAverageDurationCounter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

@Log4j2
public class InMemoryCacheService<K, V> implements CacheService<K, V> {

    private static final int DEFAULT_CAPACITY = 100_000;

    private final int capacity;

    private final ConcurrentMap<K, Entity<K, V>> map;
    private final PriorityBlockingQueue<Entity<K, V>> queue;

    private final Lock readLock;
    private final Lock writeLock;

    private final BiConsumer<K, V> removeListener;

    private final ScheduledExecutorService scheduledExecutorService;
    private final long evictionDelayMillis;

    private final RunningAverageDurationCounter putAverageDurationCounter;
    private final EvictionCounter evictionCounter;

    public InMemoryCacheService(int capacity,
                                long evictionDelay,
                                TimeUnit evictionDelayUnit,
                                BiConsumer<K, V> evictionListener) {
        this.capacity = (capacity > 0) ? capacity : DEFAULT_CAPACITY;
        this.removeListener = evictionListener;

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();

        this.map = new ConcurrentHashMap<>();
        this.queue = new PriorityBlockingQueue<>(capacity, Comparator.comparingLong(Entity::getUsageCounter));

        this.evictionDelayMillis = evictionDelayUnit.toMillis(evictionDelay);
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        startEvictionSchedule(evictionDelay, evictionDelayUnit);

        putAverageDurationCounter = new RunningAverageDurationCounter();
        evictionCounter = new EvictionCounter();
    }

    @Override
    public V get(K key) {
        try {
            readLock.lock();
            return Optional.ofNullable(map.get(key))
                    .map(Entity::getValue)
                    .orElse(null);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        long start = System.nanoTime();
        try {
            writeLock.lock();
            
            while (isCacheAtCapacity()) {
                performFrequencyEviction();
            }

            Entity<K, V> entity = new Entity<>(key, value);
            map.put(key, entity);
            queue.add(entity);
            log.debug("Entity [{}:{}] was added", key, value);

            putAverageDurationCounter.addNewDuration(start, System.nanoTime());
            log.debug("Average put duration: {} ns", putAverageDurationCounter.getAverageDuration());
        } finally {
            writeLock.unlock();
        }
    }

    private boolean isCacheAtCapacity() {
        return map.size() >= capacity;
    }

    private void performFrequencyEviction() {
        Entity<K, V> entity = queue.poll();
        if (entity != null) {
            map.remove(entity.getKey());
            performPostEvictionAction(entity);
        }
    }

    private void startEvictionSchedule(long period, TimeUnit timeUnit) {
        scheduledExecutorService.scheduleAtFixedRate(this::performTimeEviction, 0, period, timeUnit);
    }

    private void performTimeEviction() {
        log.debug("Time-based eviction started");
        long start = System.nanoTime();
        int removedCounter = 0;

        try {
            writeLock.lock();
            Iterator<Map.Entry<K, Entity<K, V>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, Entity<K, V>> entry = iterator.next();
                Entity<K, V> entity = entry.getValue();
                long livedFor = Instant.now().toEpochMilli() - entity.getLastAccessedAt();
                if (livedFor >= evictionDelayMillis) {
                    iterator.remove();
                    removedCounter++;
                    performPostEvictionAction(entity);
                }
            }
        } finally {
            writeLock.unlock();
        }

        long duration = System.nanoTime() - start;
        log.debug("Time-based eviction completed in {} ns, removed {} entities", duration, removedCounter);
    }

    private void performPostEvictionAction(Entity<K, V> entity) {
        evictionCounter.count();
        log.debug("Total evictions: {}", evictionCounter.getCount());

        removeListener.accept(entity.getKey(), entity.getValue());
    }
    
    @ToString
    static class Entity<K, V> {
        private long lastAccessedAt;
        private long usageCounter;

        private final K key;
        private final V value;

        public Entity(K key, V value) {
            this.key = key;
            this.value = value;
            this.lastAccessedAt = Instant.now().toEpochMilli();
            this.usageCounter = 0;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            lastAccessedAt = Instant.now().toEpochMilli();
            usageCounter += 1;
            return value;
        }

        public long getLastAccessedAt() {
            return lastAccessedAt;
        }

        public long getUsageCounter() {
            return usageCounter;
        }
    }

}
