package com.astroai.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponseDto(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in_ms") long expiresInMs,
        @JsonProperty("user") UserSummaryDto user
) {
    public static AuthResponseDto of(String accessToken, String refreshToken, long expiresInMs, UserSummaryDto user) {
        return new AuthResponseDto(accessToken, refreshToken, "Bearer", expiresInMs, user);
    }
}
