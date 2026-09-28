package com.astroai.strength;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BhavaBalaCalculationResponseDto(
        @JsonAlias("birth_profile_id") UUID birthProfileId,
        @JsonAlias("chart_id") UUID chartId,
        @JsonAlias("utc_datetime_iso") String utcDatetimeIso,
        @JsonAlias("julian_day_ut") double julianDayUt,
        @JsonAlias("ascendant_sign") String ascendantSign,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("strongest_house") int strongestHouse,
        @JsonAlias("weakest_house") int weakestHouse,
        @JsonAlias("average_rupas") double averageRupas,
        @JsonAlias("purushartha_summaries") List<PurusharthaSummaryDto> purusharthaSummaries,
        @JsonAlias("houses") List<HouseStrengthEntryDto> houses
) {
}
