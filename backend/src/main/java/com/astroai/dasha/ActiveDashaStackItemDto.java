package com.astroai.dasha;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActiveDashaStackItemDto(
        @JsonAlias("level") int level,
        @JsonAlias("level_name") String levelName,
        @JsonAlias("planet") String planet,
        @JsonAlias("start_date_time") String startDateTime,
        @JsonAlias("end_date_time") String endDateTime,
        @JsonAlias("unclamped_start_date_time") String unclampedStartDateTime,
        @JsonAlias("duration_days") BigDecimal durationDays,
        @JsonAlias("elapsed_percentage") BigDecimal elapsedPercentage
) {
}
