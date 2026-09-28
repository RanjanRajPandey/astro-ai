package com.astroai.strength;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShadbalaCalculationResponseDto(
        @JsonAlias("birth_profile_id") UUID birthProfileId,
        @JsonAlias("chart_id") UUID chartId,
        @JsonAlias("utc_datetime_iso") String utcDatetimeIso,
        @JsonAlias("julian_day_ut") double julianDayUt,
        @JsonAlias("ascendant_sign") String ascendantSign,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("strongest_planet") String strongestPlanet,
        @JsonAlias("weakest_planet") String weakestPlanet,
        @JsonAlias("planets") List<PlanetStrengthEntryDto> planets
) {
}
