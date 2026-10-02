package com.astroai.ai.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record SendChatMessageRequestDto(
        @NotBlank @JsonProperty("message") String message,
        @JsonProperty("domain_category") String domainCategory,
        @JsonProperty("include_reasoning") Boolean includeReasoning
) {}
