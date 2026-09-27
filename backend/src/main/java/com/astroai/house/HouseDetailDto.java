package com.astroai.house;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.List;

public record HouseDetailDto(
        @JsonAlias("house_number")
        int houseNumber,
        String sign,
        @JsonAlias("sanskrit_sign")
        String sanskritSign,
        @JsonAlias("sign_index")
        int signIndex,
        @JsonAlias("lord_planet")
        String lordPlanet,
        @JsonAlias("lord_placed_in_house")
        int lordPlacedInHouse,
        @JsonAlias("lord_placed_in_sign")
        String lordPlacedInSign,
        @JsonAlias("lord_dignity")
        String lordDignity,
        @JsonAlias("degree_cusp")
        BigDecimal degreeCusp,
        @JsonAlias("sripati_cusp_longitude")
        BigDecimal sripatiCuspLongitude,
        @JsonAlias("sripati_start_longitude")
        BigDecimal sripatiStartLongitude,
        @JsonAlias("sripati_end_longitude")
        BigDecimal sripatiEndLongitude,
        List<String> occupants,
        @JsonAlias("chalit_occupants")
        List<String> chalitOccupants,
        @JsonAlias("aspects_received")
        List<HouseAspectDto> aspectsReceived,
        String purushartha,
        List<String> classifications,
        List<String> significations,
        @JsonAlias("baseline_strength_score")
        BigDecimal baselineStrengthScore,
        @JsonAlias("strength_grade")
        String strengthGrade
) {
}
