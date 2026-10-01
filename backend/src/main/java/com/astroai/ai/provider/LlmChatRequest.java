package com.astroai.ai.provider;

import com.astroai.ai.tool.AstrologyTool;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record LlmChatRequest(
        String userMessage,
        String systemInstruction,
        List<Map<String, String>> conversationHistory,
        List<AstrologyTool> tools,
        Map<String, Object> parameters
) {
    public LlmChatRequest {
        if (conversationHistory == null) {
            conversationHistory = Collections.emptyList();
        }
        if (tools == null) {
            tools = Collections.emptyList();
        }
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
    }
}
