package com.astroai.yoga;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YogaEvaluationEntryDto(
        @JsonAlias("yoga_code") String yogaCode,
        @JsonAlias("name") String name,
        @JsonAlias("sanskrit_name") String sanskritName,
        @JsonAlias("category") String category,
        @JsonAlias("definition") String definition,
        @JsonAlias({"classical_Effect", "classical_effect"}) String classicalEffect,
        @JsonAlias("required_conditions") List<String> requiredConditions,
        @JsonAlias("detected_conditions") List<String> detectedConditions,
        @JsonAlias("planets_involved") List<String> planetsInvolved,
        @JsonAlias("houses_involved") List<Integer> housesInvolved,
        @JsonAlias("status") String status,
        @JsonAlias("strength") String strength,
        @JsonAlias("is_benefic") boolean isBenefic
) {
}
