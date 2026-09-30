package com.astroai.transit;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TransitCalculationResponseDto(
        @JsonAlias("birth_profile_id") UUID birthProfileId,
        @JsonAlias("natal_utc_datetime_iso") String natalUtcDatetimeIso,
        @JsonAlias("transit_utc_datetime_iso") String transitUtcDatetimeIso,
        @JsonAlias("transit_julian_day_ut") double transitJulianDayUt,
        @JsonAlias("natal_ascendant_sign") String natalAscendantSign,
        @JsonAlias("natal_moon_sign") String natalMoonSign,
        @JsonAlias("natal_moon_nakshatra") String natalMoonNakshatra,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("favorable_transit_count") int favorableTransitCount,
        @JsonAlias("vedha_obstructed_count") int vedhaObstructedCount,
        @JsonAlias("sade_sati") SadeSatiStatusDto sadeSati,
        @JsonAlias("double_transit_houses") List<DoubleTransitHouseEntryDto> doubleTransitHouses,
        @JsonAlias("planets") List<TransitPlanetEntryDto> planets
) {
}
