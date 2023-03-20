package com.satur9er.spurt.cache.api;

public interface CacheService<K, V> {

    V get(K key);

    void put(K key, V value);

}
