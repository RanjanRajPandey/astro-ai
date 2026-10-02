package com.astroai.ai.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record ChatSessionResponseDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("birth_profile_id") UUID birthProfileId,
        @JsonProperty("title") String title,
        @JsonProperty("rolling_summary") String rollingSummary,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {}
