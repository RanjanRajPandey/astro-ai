package com.astroai.admin;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminMaintenanceController {

    private final CacheManager cacheManager;

    public AdminMaintenanceController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<Map<String, Object>> getSystemSummary() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        Runtime rt = Runtime.getRuntime();

        long totalMem = rt.totalMemory();
        long freeMem = rt.freeMemory();
        long usedMem = totalMem - freeMem;
        long maxMem = rt.maxMemory();

        Map<String, Object> jvmStats = new LinkedHashMap<>();
        jvmStats.put("uptimeMs", runtime.getUptime());
        jvmStats.put("startTime", Instant.ofEpochMilli(runtime.getStartTime()).toString());
        jvmStats.put("availableProcessors", rt.availableProcessors());
        jvmStats.put("totalMemoryMb", totalMem / (1024 * 1024));
        jvmStats.put("freeMemoryMb", freeMem / (1024 * 1024));
        jvmStats.put("usedMemoryMb", usedMem / (1024 * 1024));
        jvmStats.put("maxMemoryMb", maxMem / (1024 * 1024));
        jvmStats.put("threadCount", threads.getThreadCount());
        jvmStats.put("peakThreadCount", threads.getPeakThreadCount());

        List<Map<String, Object>> cacheList = new ArrayList<>();
        if (cacheManager != null) {
            for (String name : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(name);
                Map<String, Object> cInfo = new LinkedHashMap<>();
                cInfo.put("name", name);
                cInfo.put("active", cache != null);
                if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine) {
                    cInfo.put("estimatedSize", caffeine.estimatedSize());
                    cInfo.put("hitCount", caffeine.stats().hitCount());
                    cInfo.put("missCount", caffeine.stats().missCount());
                    cInfo.put("hitRate", caffeine.stats().hitRate());
                }
                cacheList.add(cInfo);
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        response.put("jvm", jvmStats);
        response.put("caches", cacheList);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache(@RequestParam(required = false) String cacheName) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("timestamp", Instant.now().toString());

        if (cacheName != null && !cacheName.isBlank()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                res.put("cleared", List.of(cacheName));
                res.put("message", "Cache '" + cacheName + "' successfully evicted.");
            } else {
                res.put("cleared", List.of());
                res.put("message", "Cache '" + cacheName + "' not found.");
            }
        } else {
            List<String> cleared = new ArrayList<>();
            for (String name : cacheManager.getCacheNames()) {
                Cache c = cacheManager.getCache(name);
                if (c != null) {
                    c.clear();
                    cleared.add(name);
                }
            }
            res.put("cleared", cleared);
            res.put("message", "All caches successfully evicted (" + cleared.size() + " caches).");
        }

        return ResponseEntity.ok(res);
    }
}
