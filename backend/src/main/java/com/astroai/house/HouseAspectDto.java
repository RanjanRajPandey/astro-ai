package com.astroai.house;

import com.fasterxml.jackson.annotation.JsonAlias;

public record HouseAspectDto(
        @JsonAlias("source_planet")
        String sourcePlanet,
        @JsonAlias("source_house")
        int sourceHouse,
        @JsonAlias("source_sign")
        String sourceSign,
        @JsonAlias("aspect_house_distance")
        int aspectHouseDistance,
        @JsonAlias("aspect_type")
        String aspectType,
        String rule
) {
}
