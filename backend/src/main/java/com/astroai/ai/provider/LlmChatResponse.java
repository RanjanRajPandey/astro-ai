package com.astroai.ai.provider;

import java.util.Collections;
import java.util.List;

public record LlmChatResponse(
        String content,
        String provider,
        String model,
        String finishReason,
        List<LlmToolCall> toolCalls,
        int promptTokens,
        int completionTokens
) {
    public static LlmChatResponse ofText(String content, String provider, String model) {
        return new LlmChatResponse(
                content,
                provider,
                model,
                "stop",
                Collections.emptyList(),
                150,
                250
        );
    }
}
