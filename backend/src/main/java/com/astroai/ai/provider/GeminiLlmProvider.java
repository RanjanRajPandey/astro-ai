package com.astroai.ai.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeminiLlmProvider implements LlmProvider {

    private final String apiKey;
    private final String model;
    private final MockLlmProvider mockFallback;

    public GeminiLlmProvider(
            @Value("${astroai.ai.gemini.api-key:#{null}}") String apiKey,
            @Value("${astroai.ai.gemini.model:gemini-1.5-flash}") String model,
            MockLlmProvider mockFallback
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.mockFallback = mockFallback;
    }

    @Override
    public String getProviderName() {
        return "GEMINI";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public LlmChatResponse generateResponse(LlmChatRequest request) {
        if (!isAvailable()) {
            return mockFallback.generateResponse(request);
        }
        return mockFallback.generateResponse(request);
    }
}
