package com.astroai.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank @Email @JsonProperty("email") String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") @JsonProperty("password") String password,
        @NotBlank @JsonProperty("full_name") String fullName,
        @JsonProperty("role") String role
) {}
