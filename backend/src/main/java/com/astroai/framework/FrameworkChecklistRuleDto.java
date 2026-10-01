package com.astroai.framework;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FrameworkChecklistRuleDto(
        @JsonAlias("rule_code") String ruleCode,
        @JsonAlias("factor_category") String factorCategory,
        @JsonAlias("description") String description,
        @JsonAlias("classical_reference") String classicalReference,
        @JsonAlias("weight") double weight
) {
}
