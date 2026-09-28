package com.astroai.aspect;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HouseAspectEntryDto(
        @JsonAlias("source_planet") String sourcePlanet,
        @JsonAlias("source_sign") String sourceSign,
        @JsonAlias("source_house") int sourceHouse,
        @JsonAlias("target_house") int targetHouse,
        @JsonAlias("target_sign") String targetSign,
        @JsonAlias("house_offset") int houseOffset,
        @JsonAlias("aspect_type") String aspectType,
        @JsonAlias("is_full_aspect") boolean isFullAspect,
        @JsonAlias("is_special_aspect") boolean isSpecialAspect,
        @JsonAlias("pada_fraction") String padaFraction,
        @JsonAlias("virupa_strength") double virupaStrength,
        @JsonAlias("aspect_nature") String aspectNature,
        @JsonAlias("rule_applied") String ruleApplied
) {
}
