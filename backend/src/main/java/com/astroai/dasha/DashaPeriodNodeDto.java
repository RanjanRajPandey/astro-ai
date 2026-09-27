package com.astroai.dasha;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DashaPeriodNodeDto(
        @JsonAlias("planet") String planet,
        @JsonAlias("level") int level,
        @JsonAlias("level_name") String levelName,
        @JsonAlias("start_date_time") String startDateTime,
        @JsonAlias("end_date_time") String endDateTime,
        @JsonAlias("unclamped_start_date_time") String unclampedStartDateTime,
        @JsonAlias("duration_days") BigDecimal durationDays,
        @JsonAlias("duration_years") BigDecimal durationYears,
        @JsonAlias("is_currently_active") boolean isCurrentlyActive,
        @JsonAlias("is_birth_balance_period") boolean isBirthBalancePeriod,
        @JsonAlias("sub_periods") List<DashaPeriodNodeDto> subPeriods
) {
}
