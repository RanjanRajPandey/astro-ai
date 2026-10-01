package com.astroai.framework;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionClassificationResponseDto(
        @JsonAlias("framework_version") String frameworkVersion,
        @JsonAlias("question_text") String questionText,
        @JsonAlias("primary_category") String primaryCategory,
        @JsonAlias("secondary_category") String secondaryCategory,
        @JsonAlias("confidence_score") double confidenceScore,
        @JsonAlias("matched_keywords") List<String> matchedKeywords,
        @JsonAlias("active_framework") AnalysisFrameworkDefinitionDto activeFramework,
        @JsonAlias("all_frameworks") List<AnalysisFrameworkDefinitionDto> allFrameworks
) {
}
