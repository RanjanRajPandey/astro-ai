package com.astroai.security;

import com.astroai.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private int maxRequestsPerWindow = 120; // 120 requests per minute by default
    private long windowMillis = 60_000L;

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setLimitForTesting(int maxRequests, long windowMs) {
        this.maxRequestsPerWindow = maxRequests;
        this.windowMillis = windowMs;
        this.buckets.clear();
    }

    public void resetLimits() {
        this.maxRequestsPerWindow = 120;
        this.windowMillis = 60_000L;
        this.buckets.clear();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Rate limit sensitive calculation and auth endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/api/ai/")) {
            String clientIp = getClientIp(request);
            TokenBucket bucket = buckets.computeIfAbsent(
                    clientIp + ":" + (path.startsWith("/api/auth/") ? "auth" : "ai"),
                    k -> new TokenBucket(maxRequestsPerWindow, windowMillis)
            );

            if (!bucket.tryConsume()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", "60");
                ApiResponse<Void> errorResp = ApiResponse.error("Rate limit exceeded. Please wait 60 seconds before making further requests.");
                response.getWriter().write(objectMapper.writeValueAsString(errorResp));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    public static class TokenBucket {
        private final int capacity;
        private final long refillWindowMs;
        private int tokens;
        private long lastRefillTime;

        public TokenBucket(int capacity, long refillWindowMs) {
            this.capacity = capacity;
            this.refillWindowMs = refillWindowMs;
            this.tokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTime >= refillWindowMs) {
                tokens = capacity;
                lastRefillTime = now;
            }
        }
    }
}
