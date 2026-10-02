package com.astroai.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record UserSummaryDto(
        @JsonProperty("id") UUID id,
        @JsonProperty("email") String email,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("role") String role
) {}
