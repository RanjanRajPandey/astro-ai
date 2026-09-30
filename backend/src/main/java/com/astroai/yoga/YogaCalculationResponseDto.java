package com.astroai.yoga;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YogaCalculationResponseDto(
        @JsonAlias("birth_profile_id") UUID birthProfileId,
        @JsonAlias("chart_id") UUID chartId,
        @JsonAlias("utc_datetime_iso") String utcDatetimeIso,
        @JsonAlias("julian_day_ut") double julianDayUt,
        @JsonAlias("ascendant_sign") String ascendantSign,
        @JsonAlias("moon_sign") String moonSign,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("active_yoga_count") int activeYogaCount,
        @JsonAlias("active_dosha_count") int activeDoshaCount,
        @JsonAlias("mitigated_count") int mitigatedCount,
        @JsonAlias("total_evaluated_count") int totalEvaluatedCount,
        @JsonAlias("active_yogas") List<YogaEvaluationEntryDto> activeYogas,
        @JsonAlias("all_evaluated_yogas") List<YogaEvaluationEntryDto> allEvaluatedYogas
) {
}
