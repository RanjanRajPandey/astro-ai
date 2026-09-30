package com.astroai.temporal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DomainWindowEvaluationDto(
        @JsonAlias("domain_code") String domainCode,
        @JsonAlias("domain_title") String domainTitle,
        @JsonAlias("primary_houses") List<Integer> primaryHouses,
        @JsonAlias("natal_promise_score") double natalPromiseScore,
        @JsonAlias("dasha_activation_score") double dashaActivationScore,
        @JsonAlias("transit_confluence_score") double transitConfluenceScore,
        @JsonAlias("overall_confluence_score") double overallConfluenceScore,
        @JsonAlias("window_classification") String windowClassification,
        @JsonAlias("double_transit_triggered") boolean doubleTransitTriggered,
        @JsonAlias("supporting_factors") List<String> supportingFactors,
        @JsonAlias("challenging_factors") List<String> challengingFactors
) {
}
