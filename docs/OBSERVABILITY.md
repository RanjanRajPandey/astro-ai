# Astro-AI: Observability, Monitoring & Operations Guide

This guide details the complete observability architecture, Prometheus metrics exposition, Grafana dashboards, distributed tracing via MDC correlation IDs, and operational maintenance procedures for the **Astro-AI** enterprise platform.

---

## 1. Observability Architecture

```text
               HTTP Requests (Clients & Services)
                              │  [X-Correlation-ID / X-Request-ID]
                              ▼
                   ┌─────────────────────┐
                   │ CorrelationIdFilter │
                   │  (Places in MDC)    │
                   └──────────┬──────────┘
                              │
          ┌───────────────────┴───────────────────┐
          │                                       │
          ▼                                       ▼
┌───────────────────────────────┐     ┌────────────────────────────────┐
│   Spring Boot Core Backend    │     │  Python Astrology Engine       │
│  - JVM & HikariCP Metrics     │     │  - Ephemeris Calculation Stats │
│  - Caffeine Cache Metrics     │     │  - LRU Cache Hit/Miss Counters │
│  - Async Rolling Logback      │     │  - Request Timing Middleware   │
│  - /actuator/prometheus       │     │  - /metrics                    │
└──────────────┬────────────────┘     └───────────────┬────────────────┘
               │                                      │
               │ (Scrape: 5s interval)                │ (Scrape: 5s interval)
               ▼                                      ▼
     ┌────────────────────────────────────────────────────────┐
     │            Prometheus Time-Series Database             │
     │  - Scrapes metrics from backend & engine targets       │
     │  - Evaluates alerting rules (alert.rules.yml)          │
     └────────────────────────┬───────────────────────────────┘
                              │
                              ▼
     ┌────────────────────────────────────────────────────────┐
     │             Grafana Visualization Suite                │
     │  - Pre-provisioned Dashboards & Datasources            │
     │  - Real-time RPS, Latency p95, Cache Ratios, JVM Heap  │
     └────────────────────────────────────────────────────────┘
```

---

## 2. Distributed Tracing & Correlation IDs

Every incoming HTTP request passing through the Spring Boot API is intercepted by the [`CorrelationIdFilter`](file:///C:/Users/ranja/.gemini/antigravity/scratch/astro-ai/backend/src/main/java/com/astroai/config/CorrelationIdFilter.java):

1. **Extraction / Generation**:
   - Inspects `X-Correlation-ID` or `X-Request-ID`.
   - If not provided by client/gateway, generates a cryptographically random UUID v4.
2. **Context Binding**:
   - Injects the ID into SLF4J MDC under key `correlationId`.
   - Returns the ID in the HTTP response header `X-Correlation-ID`.
3. **Structured Logging**:
   - Asynchronous Logback appenders output `[cid=<correlationId>]` on every single log line.
   - Ensures any request can be traced across asynchronous threads, database queries, and downstream calculations.

---

## 3. Metrics Catalog

### Spring Boot Backend Actuator (`/actuator/prometheus`)

| Metric Name | Type | Description |
| :--- | :--- | :--- |
| `http_server_requests_seconds_bucket` | Histogram | Request latency distributions partitioned by URI, HTTP method, and status. |
| `cache_gets_total` | Counter | Caffeine cache lookups partitioned by cache name (`chartsCache`, `dashaCache`, `transitsCache`, etc.) and result (`hit` / `miss`). |
| `cache_evictions_total` | Counter | Number of cache items evicted due to TTL (60 min) or size limit (5,000 entries). |
| `jvm_memory_used_bytes` | Gauge | JVM heap and non-heap memory utilization. |
| `jvm_gc_pause_seconds_count` | Counter | Garbage collection pause frequency and durations. |
| `hikaricp_connections_active` | Gauge | Number of active PostgreSQL database connections currently executing queries. |
| `hikaricp_connections_idle` | Gauge | Idle database connections ready in the Hikari pool. |

### Python Swiss Ephemeris Engine (`/metrics`)

| Metric Name | Type | Description |
| :--- | :--- | :--- |
| `astro_engine_uptime_seconds` | Gauge | Total process uptime in seconds. |
| `astro_engine_requests_total` | Counter | Total astronomical calculation requests handled by FastAPI. |
| `astro_ephemeris_cache_hits_total` | Counter | Aggregated cache hits across Ayanamsha, Ascendant, and Planetary bodies. |
| `astro_ephemeris_cache_misses_total`| Counter | Aggregated cache misses across Swiss Ephemeris calculations. |
| `astro_ephemeris_cache_current_size`| Gauge | Total entries currently held in memory LRU caches. |
| `astro_ephemeris_cache_hit_ratio`   | Gauge | Cache hit ratio (\(0.0000 - 1.0000\)). Target: > 0.85 under load. |

---

## 4. Prometheus Alerting Rules

The platform ships with pre-configured operational alerts in `monitoring/prometheus/alert.rules.yml`:

- **`AstroServiceDown`** (Critical): Triggers when any backend or engine instance is unreachable for > 1 minute.
- **`HighCalculationLatency`** (Warning): Triggers when p95 request duration exceeds 2.5 seconds over a 5-minute window.
- **`LowCacheHitRatio`** (Warning): Triggers when the Caffeine cache hit ratio drops below 40% under sustained load.
- **`HighServerErrorRate`** (Critical): Triggers when HTTP 5xx errors exceed 5% of total requests over 2 minutes.
- **`HighJvmHeapUsage`** (Warning): Triggers when JVM heap memory exceeds 85% capacity for > 3 minutes.

---

## 5. Grafana Operations Dashboard

The pre-built dashboard `Astro-AI Platform Operations Overview` (`monitoring/grafana/dashboards/astro-ai-overview.json`) provides real-time visualization of:

1. **System Health Status**: Dual stat indicators for Backend and Calculation Engine.
2. **Ephemeris LRU Hit Ratio**: Gauge panel with visual color thresholds (Red < 50%, Yellow 50-80%, Green > 80%).
3. **Request Throughput (RPS)**: Stacked timeseries showing requests/sec grouped by HTTP status.
4. **Latency Percentiles**: Live tracking of p50, p95, and p99 response times.
5. **JVM Heap Utilization**: Live heap memory usage against configured container limits.
6. **HikariCP Connection Pool**: Active vs. idle connections to monitor database saturation.

### Accessing Grafana
When launched with `docker compose -f docker-compose.monitoring.yml up -d`:
- URL: `http://localhost:3001`
- Default User: `admin` / Password: `${GRAFANA_ADMIN_PASSWORD:-admin123}`

---

## 6. Admin Maintenance Endpoints

Astro-AI includes dedicated administrative maintenance endpoints:

### 1. System Operational Metrics Summary
- **Endpoint**: `GET /api/admin/metrics/summary`
- **Response**:
```json
{
  "status": "UP",
  "timestamp": "2026-10-02T14:25:00Z",
  "jvm": {
    "uptimeMs": 1425890,
    "availableProcessors": 8,
    "totalMemoryMb": 512,
    "freeMemoryMb": 384,
    "usedMemoryMb": 128,
    "maxMemoryMb": 2048,
    "threadCount": 28
  },
  "caches": [
    { "name": "chartsCache", "active": true, "estimatedSize": 14, "hitCount": 42, "hitRate": 0.875 },
    { "name": "dashaCache", "active": true, "estimatedSize": 8, "hitCount": 26, "hitRate": 0.928 }
  ]
}
```

### 2. Dynamic Cache Eviction
- **Evict All Caches**: `POST /api/admin/cache/clear`
- **Evict Single Cache**: `POST /api/admin/cache/clear?cacheName=transitsCache`

### 3. Swiss Ephemeris Engine Cache Operations
- **Inspect Ephemeris Stats**: `GET /api/v1/admin/cache/stats` (on engine port 8000)
- **Evict Ephemeris Caches**: `POST /api/v1/admin/cache/clear` (on engine port 8000)
