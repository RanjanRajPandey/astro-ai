package com.astroai.aspect;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MutualAspectSummaryDto(
        @JsonAlias("planet_a") String planetA,
        @JsonAlias("house_a") int houseA,
        @JsonAlias("sign_a") String signA,
        @JsonAlias("planet_b") String planetB,
        @JsonAlias("house_b") int houseB,
        @JsonAlias("sign_b") String signB,
        @JsonAlias("relationship_type") String relationshipType,
        @JsonAlias("a_to_b_aspect_type") String aToBAspectType,
        @JsonAlias("b_to_a_aspect_type") String bToAAspectType,
        @JsonAlias("combined_virupa_strength") double combinedVirupaStrength,
        @JsonAlias("exact_orb_deg") double exactOrbDeg,
        @JsonAlias("description") String description
) {
}
