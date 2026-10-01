package com.astroai.evidence;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvidenceItemDto(
        @JsonProperty("factor") @JsonAlias("factor") String factor,
        @JsonProperty("category") @JsonAlias("category") String category,
        @JsonProperty("observation") @JsonAlias("observation") String observation,
        @JsonProperty("rule_reference") @JsonAlias("rule_reference") String ruleReference,
        @JsonProperty("finding") @JsonAlias("finding") String finding,
        @JsonProperty("weight") @JsonAlias("weight") Double weight
) {
}
