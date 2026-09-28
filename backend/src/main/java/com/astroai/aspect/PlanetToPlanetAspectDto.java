package com.astroai.aspect;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlanetToPlanetAspectDto(
        @JsonAlias("source_planet") String sourcePlanet,
        @JsonAlias("source_house") int sourceHouse,
        @JsonAlias("source_sign") String sourceSign,
        @JsonAlias("source_longitude") double sourceLongitude,
        @JsonAlias("target_planet") String targetPlanet,
        @JsonAlias("target_house") int targetHouse,
        @JsonAlias("target_sign") String targetSign,
        @JsonAlias("target_longitude") double targetLongitude,
        @JsonAlias("house_offset") int houseOffset,
        @JsonAlias("angular_separation_deg") double angularSeparationDeg,
        @JsonAlias("orb_from_exact_aspect_deg") double orbFromExactAspectDeg,
        @JsonAlias("aspect_type") String aspectType,
        @JsonAlias("is_full_aspect") boolean isFullAspect,
        @JsonAlias("is_special_aspect") boolean isSpecialAspect,
        @JsonAlias("pada_fraction") String padaFraction,
        @JsonAlias("virupa_strength") double virupaStrength,
        @JsonAlias("sphuta_virupa_strength") double sphutaVirupaStrength,
        @JsonAlias("aspect_nature") String aspectNature,
        @JsonAlias("rule_applied") String ruleApplied
) {
}
