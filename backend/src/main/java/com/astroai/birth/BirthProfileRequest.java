package com.astroai.birth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record BirthProfileRequest(
        UUID userId,
        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,
        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,
        LocalTime timeOfBirth,
        @NotBlank(message = "Place of birth is required")
        @Size(max = 255, message = "Place of birth must be at most 255 characters")
        String placeOfBirth,
        @NotBlank(message = "Gender is required")
        String gender,
        Double latitude,
        Double longitude,
        String timezone
) {
}
