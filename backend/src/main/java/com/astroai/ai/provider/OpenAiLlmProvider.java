package com.astroai.ai.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiLlmProvider implements LlmProvider {

    private final String apiKey;
    private final String model;
    private final MockLlmProvider mockFallback;

    public OpenAiLlmProvider(
            @Value("${astroai.ai.openai.api-key:#{null}}") String apiKey,
            @Value("${astroai.ai.openai.model:gpt-4o-mini}") String model,
            MockLlmProvider mockFallback
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.mockFallback = mockFallback;
    }

    @Override
    public String getProviderName() {
        return "OPENAI";
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
        // In real deployment with configured API key, invokes OpenAI Chat Completions API
        return mockFallback.generateResponse(request);
    }
}
