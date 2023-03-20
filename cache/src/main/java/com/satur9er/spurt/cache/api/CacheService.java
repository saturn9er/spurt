package com.satur9er.jmp.cache.api;

public interface JmpCacheService<K, V> {
    V get(K key);
    void put(K key, V value);
}
