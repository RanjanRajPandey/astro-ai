package com.astroai.observability;

import com.astroai.config.CorrelationIdFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ObservabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Test
    @DisplayName("Verify /actuator/prometheus exports valid Micrometer Prometheus metrics")
    void testPrometheusEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(containsString("jvm_memory_used_bytes")))
                .andExpect(content().string(containsString("jvm_threads_live_threads")));
    }

    @Test
    @DisplayName("Verify CorrelationIdFilter generates and propagates X-Correlation-ID header")
    void testCorrelationIdHeaderPropagation() throws Exception {
        // Request without incoming correlation ID should have one generated
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(CorrelationIdFilter.CORRELATION_ID_HEADER));

        // Request with incoming correlation ID should preserve it
        String customId = "trace-test-12345";
        mockMvc.perform(get("/api/health")
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, customId))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, customId));
    }

    @Test
    @DisplayName("Verify Admin Maintenance metrics summary endpoint returns JVM and Cache details")
    void testAdminMetricsSummary() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.jvm.availableProcessors", greaterThan(0)))
                .andExpect(jsonPath("$.jvm.totalMemoryMb", greaterThan(0)))
                .andExpect(jsonPath("$.caches", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Verify Admin Maintenance cache clearing evicts targeted Caffeine cache")
    void testAdminCacheEviction() throws Exception {
        // Populate sample entry into geocodingCache
        Cache cache = cacheManager.getCache("geocodingCache");
        assertThat(cache).isNotNull();
        cache.put("test_key", "test_value");
        assertThat(cache.get("test_key")).isNotNull();

        // Evict specific cache via admin endpoint
        mockMvc.perform(post("/api/admin/cache/clear?cacheName=geocodingCache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cleared", contains("geocodingCache")));

        // Verify key is evicted
        assertThat(cache.get("test_key")).isNull();
    }
}
