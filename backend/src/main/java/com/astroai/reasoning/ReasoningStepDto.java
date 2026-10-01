package com.astroai.reasoning;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReasoningStepDto(
        @JsonProperty("step_order") @JsonAlias("stepOrder") int stepOrder,
        @JsonProperty("step_type") @JsonAlias("stepType") String stepType,
        @JsonProperty("title") @JsonAlias("title") String title,
        @JsonProperty("verdict") @JsonAlias("verdict") String verdict,
        @JsonProperty("confidence_score") @JsonAlias("confidenceScore") double confidenceScore,
        @JsonProperty("narrative") @JsonAlias("narrative") String narrative,
        @JsonProperty("linked_factors") @JsonAlias("linkedFactors") List<String> linkedFactors,
        @JsonProperty("shastra_citations") @JsonAlias("shastraCitations") List<String> shastraCitations
) {
}
