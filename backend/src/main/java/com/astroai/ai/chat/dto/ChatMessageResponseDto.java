package com.astroai.ai.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record ChatMessageResponseDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("chat_session_id") UUID chatSessionId,
        @JsonProperty("sender_role") String senderRole,
        @JsonProperty("message_content") String messageContent,
        @JsonProperty("explainability_trace") ExplainabilityTraceDto explainabilityTrace,
        @JsonProperty("prompt_tokens") int promptTokens,
        @JsonProperty("completion_tokens") int completionTokens,
        @JsonProperty("created_at") Instant createdAt
) {}
