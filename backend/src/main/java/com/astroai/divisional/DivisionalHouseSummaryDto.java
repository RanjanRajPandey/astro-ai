package com.astroai.divisional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DivisionalHouseSummaryDto(
        @JsonAlias("house_number") int houseNumber,
        @JsonAlias("sign") String sign,
        @JsonAlias("sanskrit_sign") String sanskritSign,
        @JsonAlias("sign_index") int signIndex,
        @JsonAlias("lord_planet") String lordPlanet,
        @JsonAlias("occupants") List<String> occupants
) {
}
