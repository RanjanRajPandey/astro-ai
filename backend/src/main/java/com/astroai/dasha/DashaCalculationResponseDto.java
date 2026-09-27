package com.astroai.dasha;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DashaCalculationResponseDto(
        UUID birthProfileId,
        @JsonAlias("birth_utc_datetime_iso") String birthUtcDatetimeIso,
        @JsonAlias("target_utc_datetime_iso") String targetUtcDatetimeIso,
        @JsonAlias("julian_day_ut") double julianDayUt,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("ayanamsha_value") BigDecimal ayanamshaValue,
        @JsonAlias("year_length_days") BigDecimal yearLengthDays,
        @JsonAlias("moon_longitude") BigDecimal moonLongitude,
        @JsonAlias("janma_nakshatra") String janmaNakshatra,
        @JsonAlias("janma_pada") int janmaPada,
        @JsonAlias("birth_dasha_lord") String birthDashaLord,
        @JsonAlias("moon_elapsed_fraction") BigDecimal moonElapsedFraction,
        @JsonAlias("moon_remaining_fraction") BigDecimal moonRemainingFraction,
        @JsonAlias("birth_balance_years") BigDecimal birthBalanceYears,
        @JsonAlias("birth_balance_days") BigDecimal birthBalanceDays,
        @JsonAlias("birth_balance_formatted") String birthBalanceFormatted,
        @JsonAlias("active_stack") List<ActiveDashaStackItemDto> activeStack,
        @JsonAlias("active_sookshma_periods") List<DashaPeriodNodeDto> activeSookshmaPeriods,
        @JsonAlias("active_prana_periods") List<DashaPeriodNodeDto> activePranaPeriods,
        @JsonAlias("mahadashas") List<DashaPeriodNodeDto> mahadashas
) {
}
