package com.astroai.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank @Email @JsonProperty("email") String email,
        @NotBlank @JsonProperty("password") String password
) {}
