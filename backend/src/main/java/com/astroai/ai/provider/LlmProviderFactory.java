package com.astroai.ai.provider;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LlmProviderFactory {

    private final String configuredProviderName;
    private final List<LlmProvider> providers;
    private final MockLlmProvider mockFallback;

    public LlmProviderFactory(
            @Value("${astroai.ai.provider:MOCK}") String configuredProviderName,
            List<LlmProvider> providers,
            MockLlmProvider mockFallback
    ) {
        this.configuredProviderName = configuredProviderName != null ? configuredProviderName.trim().toUpperCase() : "MOCK";
        this.providers = providers;
        this.mockFallback = mockFallback;
    }

    public LlmProvider getProvider() {
        for (LlmProvider p : providers) {
            if (p.getProviderName().equalsIgnoreCase(configuredProviderName) && p.isAvailable()) {
                return p;
            }
        }
        return mockFallback;
    }

    public String getConfiguredProviderName() {
        return configuredProviderName;
    }
}
