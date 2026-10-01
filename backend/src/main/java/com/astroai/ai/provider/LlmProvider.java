package com.astroai.ai.provider;

public interface LlmProvider {

    String getProviderName();

    boolean isAvailable();

    LlmChatResponse generateResponse(LlmChatRequest request);
}
