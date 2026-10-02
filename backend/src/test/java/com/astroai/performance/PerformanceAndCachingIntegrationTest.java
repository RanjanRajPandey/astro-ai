package com.astroai.performance;

import com.astroai.birth.BirthProfileRequest;
import com.astroai.birth.BirthProfileResponse;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.ChartService;
import com.astroai.chart.KundliChartResponseDto;
import com.astroai.config.CacheConfig;
import com.astroai.location.LocationService;
import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PerformanceAndCachingIntegrationTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ChartService chartService;

    @Autowired
    private BirthProfileService birthProfileService;

    @Test
    void verifiesCacheManagerContainsAllRequiredPerformanceCaches() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).contains(
                CacheConfig.GEOCODING_CACHE,
                CacheConfig.CHARTS_CACHE,
                CacheConfig.DIVISIONAL_CACHE,
                CacheConfig.DASHA_CACHE,
                CacheConfig.ASPECTS_CACHE,
                CacheConfig.STRENGTH_CACHE,
                CacheConfig.BHAVA_BALA_CACHE,
                CacheConfig.TRANSITS_CACHE,
                CacheConfig.FRAMEWORKS_CACHE
        );
    }

    @Test
    void verifiesGeocodingCacheHitsAndStats() {
        org.springframework.cache.Cache springCache = cacheManager.getCache(CacheConfig.GEOCODING_CACHE);
        assertThat(springCache).isNotNull();
        springCache.clear();

        // First lookup (cache miss -> stores in cache)
        List<LocationService.GazetteerCity> res1 = locationService.searchCities("Kushinagar");
        assertThat(res1).isNotEmpty();

        // Verify cache entry now exists
        org.springframework.cache.Cache.ValueWrapper wrapper = springCache.get("kushinagar");
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.get()).isEqualTo(res1);

        // Second lookup (cache hit)
        List<LocationService.GazetteerCity> res2 = locationService.searchCities("Kushinagar");
        assertThat(res2).isEqualTo(res1);

        if (springCache instanceof CaffeineCache caffeineCache) {
            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
            assertThat(nativeCache.stats().hitCount()).isGreaterThanOrEqualTo(1);
        }
    }

    @Test
    void verifiesChartCacheAndEvictionOnProfileUpdate() {
        // 1. Create a birth profile
        BirthProfileRequest createReq = new BirthProfileRequest(
                null,
                "Cache Test Native",
                LocalDate.of(2004, 8, 22),
                LocalTime.of(18, 5, 0),
                "Kushinagar, Uttar Pradesh",
                "MALE",
                null,
                null,
                null
        );
        BirthProfileResponse profile = birthProfileService.createProfile(createReq);
        UUID profileId = profile.id();

        org.springframework.cache.Cache chartsCache = cacheManager.getCache(CacheConfig.CHARTS_CACHE);
        assertThat(chartsCache).isNotNull();
        chartsCache.evict(profileId);

        // 2. First calculation -> populates cache
        KundliChartResponseDto chart1 = chartService.getD1KundliChart(profileId);
        assertThat(chart1).isNotNull();
        assertThat(chartsCache.get(profileId)).isNotNull();

        // 3. Second call -> returned from cache
        KundliChartResponseDto chart2 = chartService.getD1KundliChart(profileId);
        assertThat(chart2.chartId()).isEqualTo(chart1.chartId());

        // 4. Update profile -> triggers @CacheEvict
        BirthProfileRequest updateReq = new BirthProfileRequest(
                profile.userId(),
                "Cache Test Native (Updated)",
                LocalDate.of(2004, 8, 22),
                LocalTime.of(18, 10, 0),
                "Kushinagar, Uttar Pradesh",
                "MALE",
                null,
                null,
                null
        );
        birthProfileService.updateProfile(profileId, updateReq);

        // Cache entry must be evicted
        assertThat(chartsCache.get(profileId)).isNull();

        // 5. Clean up
        birthProfileService.deleteProfile(profileId);
        assertThat(chartsCache.get(profileId)).isNull();
    }
}
