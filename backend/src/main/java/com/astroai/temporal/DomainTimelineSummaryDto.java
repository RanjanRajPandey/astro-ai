package com.astroai.temporal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DomainTimelineSummaryDto(
        @JsonAlias("domain_code") String domainCode,
        @JsonAlias("domain_title") String domainTitle,
        @JsonAlias("primary_houses") List<Integer> primaryHouses,
        @JsonAlias("karaka_planets") List<String> karakaPlanets,
        @JsonAlias("natal_promise_score") double natalPromiseScore,
        @JsonAlias("average_confluence_score") double averageConfluenceScore,
        @JsonAlias("peak_score") double peakScore,
        @JsonAlias("peak_window_label") String peakWindowLabel,
        @JsonAlias("peak_window_start_utc") String peakWindowStartUtc,
        @JsonAlias("peak_window_end_utc") String peakWindowEndUtc,
        @JsonAlias("current_classification") String currentClassification,
        @JsonAlias("executive_summary") String executiveSummary
) {
}
