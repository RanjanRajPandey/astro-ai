package com.astroai.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String GEOCODING_CACHE = "geocodingCache";
    public static final String CHARTS_CACHE = "chartsCache";
    public static final String DIVISIONAL_CACHE = "divisionalCache";
    public static final String DASHA_CACHE = "dashaCache";
    public static final String ASPECTS_CACHE = "aspectsCache";
    public static final String STRENGTH_CACHE = "strengthCache";
    public static final String BHAVA_BALA_CACHE = "bhavaBalaCache";
    public static final String TRANSITS_CACHE = "transitsCache";
    public static final String FRAMEWORKS_CACHE = "frameworksCache";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                GEOCODING_CACHE,
                CHARTS_CACHE,
                DIVISIONAL_CACHE,
                DASHA_CACHE,
                ASPECTS_CACHE,
                STRENGTH_CACHE,
                BHAVA_BALA_CACHE,
                TRANSITS_CACHE,
                FRAMEWORKS_CACHE
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(5000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
