package com.astroai.divisional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DivisionalChartDto(
        @JsonAlias("varga_code") String vargaCode,
        @JsonAlias("division_number") int divisionNumber,
        @JsonAlias("sanskrit_name") String sanskritName,
        @JsonAlias("title") String title,
        @JsonAlias("domain_signification") String domainSignification,
        @JsonAlias("ascendant_sign") String ascendantSign,
        @JsonAlias("ascendant_sanskrit_sign") String ascendantSanskritSign,
        @JsonAlias("ascendant_sign_index") int ascendantSignIndex,
        @JsonAlias("ascendant_lord") String ascendantLord,
        @JsonAlias("is_ascendant_vargottama") boolean isAscendantVargottama,
        @JsonAlias("vargottama_planets") List<String> vargottamaPlanets,
        @JsonAlias("planets") List<DivisionalPlanetPlacementDto> planets,
        @JsonAlias("houses") List<DivisionalHouseSummaryDto> houses
) {
}
