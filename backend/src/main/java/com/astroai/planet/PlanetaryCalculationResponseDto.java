package com.astroai.planet;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PlanetaryCalculationResponseDto(
        UUID birthProfileId,
        UUID chartId,
        @JsonAlias("utc_datetime_iso")
        String utcDatetimeIso,
        @JsonAlias("julian_day_ut")
        BigDecimal julianDayUt,
        @JsonAlias("ayanamsha_type")
        String ayanamshaType,
        @JsonAlias("ayanamsha_value")
        BigDecimal ayanamshaValue,
        @JsonAlias("node_type")
        String nodeType,
        @JsonAlias("ascendant_longitude")
        BigDecimal ascendantLongitude,
        @JsonAlias("ascendant_sign")
        String ascendantSign,
        @JsonAlias("ascendant_sign_index")
        int ascendantSignIndex,
        @JsonAlias("ascendant_degree_in_sign")
        BigDecimal ascendantDegreeInSign,
        @JsonAlias("ascendant_dms")
        String ascendantDms,
        List<PlanetPositionDto> planets
) {
}
