package com.astroai.planet;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;
import java.util.Map;

public record PlanetPositionDto(
        String planet,
        BigDecimal longitude,
        BigDecimal latitude,
        @JsonAlias("speed_longitude")
        BigDecimal speedLongitude,
        String sign,
        @JsonAlias("sanskrit_sign")
        String sanskritSign,
        @JsonAlias("sign_index")
        int signIndex,
        @JsonAlias("sign_lord")
        String signLord,
        @JsonAlias("degree_in_sign")
        BigDecimal degreeInSign,
        @JsonAlias("degree_dms")
        String degreeDms,
        int house,
        String nakshatra,
        @JsonAlias("nakshatra_index")
        int nakshatraIndex,
        @JsonAlias("nakshatra_lord")
        String nakshatraLord,
        int pada,
        @JsonAlias("is_retrograde")
        boolean retrograde,
        @JsonAlias("is_combust")
        boolean combust,
        @JsonAlias("angular_distance_from_sun")
        BigDecimal angularDistanceFromSun,
        @JsonAlias("is_exalted")
        boolean exalted,
        @JsonAlias("is_debilitated")
        boolean debilitated,
        @JsonAlias("is_moolatrikona")
        boolean moolatrikona,
        @JsonAlias("is_own_sign")
        boolean ownSign,
        String dignity,
        @JsonAlias("dispositor_relationship")
        String dispositorRelationship,
        @JsonAlias("planetary_relationships")
        Map<String, Map<String, String>> planetaryRelationships
) {
}
