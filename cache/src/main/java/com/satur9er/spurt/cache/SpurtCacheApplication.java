package com.satur9er.spurt.cache;

import com.satur9er.spurt.cache.api.CacheService;
import com.satur9er.spurt.cache.impl.InMemoryCacheService;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Log4j2
public class SpurtCacheApplication {

    public static void main(String[] args) {
        CacheService<String, String> myCache = new InMemoryCacheService<>(
                100000,
                10, TimeUnit.SECONDS,
                (k, v) -> log.info("Entity [{}:{}] was removed", k, v)
        );

        new Thread(() -> {
            List<String> allValuesEverExisted = new ArrayList<>();
            while (true) {
                String randomName = generateRandomString();
                allValuesEverExisted.add(randomName);
                myCache.put(randomName, generateRandomString());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public static String generateRandomString() {
        return UUID.randomUUID().toString();
    }

}
