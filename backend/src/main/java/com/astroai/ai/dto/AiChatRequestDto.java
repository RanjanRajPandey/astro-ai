package com.astroai.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.UUID;

public record AiChatRequestDto(
        @JsonProperty("birth_profile_id") UUID birthProfileId,
        @JsonProperty("user_message") String userMessage,
        @JsonProperty("domain_category") String domainCategory,
        @JsonProperty("provider") String provider,
        @JsonProperty("model") String model,
        @JsonProperty("include_reasoning") Boolean includeReasoning,
        @JsonProperty("parameters") Map<String, Object> parameters
) {}
