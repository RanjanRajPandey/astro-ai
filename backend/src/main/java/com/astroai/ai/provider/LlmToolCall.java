package com.astroai.ai.provider;

import java.util.Map;

public record LlmToolCall(
        String callId,
        String toolName,
        Map<String, Object> arguments
) {
}
