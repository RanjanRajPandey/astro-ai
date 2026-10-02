package com.astroai.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SystemHealthController {

    @Value("${astroai.ai.provider:MOCK}")
    private String aiProvider;

    @Value("${astroai.engine.base-url:http://localhost:8000}")
    private String astroEngineUrl;

    @GetMapping({"/", "/api", "/api/health"})
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
                "status", "UP",
                "service", "astro-ai-backend",
                "frontendUrl", "http://localhost:5173",
                "aiProvider", aiProvider,
                "astrologyEngineUrl", astroEngineUrl,
                "specificationVersion", "1.0.0-BPHS-LAHIRI"
        ));
    }
}
