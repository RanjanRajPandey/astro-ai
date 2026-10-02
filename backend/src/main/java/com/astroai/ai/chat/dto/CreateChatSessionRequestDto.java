package com.astroai.ai.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateChatSessionRequestDto(
        @NotNull @JsonProperty("birth_profile_id") UUID birthProfileId,
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("title") String title
) {}
