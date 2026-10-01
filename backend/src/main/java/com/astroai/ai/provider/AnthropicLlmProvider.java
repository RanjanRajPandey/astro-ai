package com.astroai.ai.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AnthropicLlmProvider implements LlmProvider {

    private final String apiKey;
    private final String model;
    private final MockLlmProvider mockFallback;

    public AnthropicLlmProvider(
            @Value("${astroai.ai.anthropic.api-key:#{null}}") String apiKey,
            @Value("${astroai.ai.anthropic.model:claude-3-5-sonnet-20241022}") String model,
            MockLlmProvider mockFallback
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.mockFallback = mockFallback;
    }

    @Override
    public String getProviderName() {
        return "ANTHROPIC";
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
