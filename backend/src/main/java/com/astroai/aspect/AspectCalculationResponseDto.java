package com.astroai.aspect;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AspectCalculationResponseDto(
        @JsonAlias("birth_profile_id") UUID birthProfileId,
        @JsonAlias("chart_id") UUID chartId,
        @JsonAlias("utc_datetime_iso") String utcDatetimeIso,
        @JsonAlias("julian_day_ut") double julianDayUt,
        @JsonAlias("ascendant_sign") String ascendantSign,
        @JsonAlias("ayanamsha_type") String ayanamshaType,
        @JsonAlias("rahu_ketu_trinal_aspects") boolean rahuKetuTrinalAspects,
        @JsonAlias("house_aspects") List<HouseAspectEntryDto> houseAspects,
        @JsonAlias("planet_aspects") List<PlanetToPlanetAspectDto> planetAspects,
        @JsonAlias("mutual_relationships") List<MutualAspectSummaryDto> mutualRelationships
) {
}
