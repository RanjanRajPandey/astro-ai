package com.astroai.ai.dto;

import com.astroai.ai.guardrail.GuardrailValidationResult;
import com.astroai.ai.provider.LlmToolCall;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record AiChatResponseDto(
        @JsonProperty("response") String response,
        @JsonProperty("provider") String provider,
        @JsonProperty("model") String model,
        @JsonProperty("guardrail_result") GuardrailValidationResult guardrailResult,
        @JsonProperty("tools_invoked") List<LlmToolCall> toolsInvoked,
        @JsonProperty("ground_truth_context") Map<String, Object> groundTruthContext,
        @JsonProperty("prompt_tokens") int promptTokens,
        @JsonProperty("completion_tokens") int completionTokens
) {
    public AiChatResponseDto {
        if (toolsInvoked == null) {
            toolsInvoked = Collections.emptyList();
        }
        if (groundTruthContext == null) {
            groundTruthContext = Collections.emptyMap();
        }
    }
}
