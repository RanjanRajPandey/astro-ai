package com.astroai.client;

import com.astroai.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/engine")
public class EngineDiagnosticController {

    private final String configuredBaseUrl;
    private final RestClient engineClient;

    public EngineDiagnosticController(
            @Value("${astroai.engine.base-url:http://localhost:8000}") String engineBaseUrl
    ) {
        this.configuredBaseUrl = engineBaseUrl;
        this.engineClient = EngineRestClientFactory.createEngineClient(engineBaseUrl);
    }

    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pingEngine() {
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("timestamp", Instant.now().toString());
        results.put("configured_url", configuredBaseUrl);
        results.put("normalized_url", EngineUrlNormalizer.normalize(configuredBaseUrl));

        try {
            String healthBody = engineClient.get()
                    .uri("/health")
                    .retrieve()
                    .body(String.class);
            results.put("status", "UP");
            results.put("response", healthBody);
            return ResponseEntity.ok(ApiResponse.ok("Astrology engine reached successfully", results));
        } catch (Exception ex) {
            results.put("status", "DOWN");
            results.put("error", ex.getMessage());
            return ResponseEntity.status(503).body(ApiResponse.error("Astrology engine probe failed: " + ex.getMessage()));
        }
    }
}
