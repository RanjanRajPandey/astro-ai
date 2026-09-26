package com.astroai.birth;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record BirthProfileResponse(
        UUID id,
        UUID userId,
        String name,
        LocalDate dateOfBirth,
        LocalTime timeOfBirth,
        boolean birthTimeAccurate,
        String placeOfBirth,
        String gender,
        BigDecimal latitude,
        BigDecimal longitude,
        String timezone,
        BigDecimal utcOffsetHours,
        Instant utcBirthTime,
        List<String> calculationWarnings,
        Instant createdAt,
        Instant updatedAt
) {
    public static BirthProfileResponse fromEntity(BirthProfile entity, List<String> warnings) {
        return new BirthProfileResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getDateOfBirth(),
                entity.getTimeOfBirth(),
                entity.isBirthTimeAccurate(),
                entity.getPlaceOfBirth(),
                entity.getGender(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getTimezone(),
                entity.getUtcOffsetHours(),
                entity.getUtcBirthTime(),
                warnings != null ? warnings : List.of(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
