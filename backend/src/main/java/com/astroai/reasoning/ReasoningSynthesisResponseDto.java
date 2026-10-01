package com.astroai.reasoning;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReasoningSynthesisResponseDto(
        @JsonProperty("analysis_session_id") @JsonAlias("analysisSessionId") UUID analysisSessionId,
        @JsonProperty("birth_profile_id") @JsonAlias("birthProfileId") UUID birthProfileId,
        @JsonProperty("framework_version") @JsonAlias("frameworkVersion") String frameworkVersion,
        @JsonProperty("question_text") @JsonAlias("questionText") String questionText,
        @JsonProperty("question_category") @JsonAlias("questionCategory") String questionCategory,
        @JsonProperty("primary_houses") @JsonAlias("primaryHouses") List<Integer> primaryHouses,
        @JsonProperty("overall_verdict") @JsonAlias("overallVerdict") String overallVerdict,
        @JsonProperty("composite_score") @JsonAlias("compositeScore") double compositeScore,
        @JsonProperty("reasoning_steps") @JsonAlias("reasoningSteps") List<ReasoningStepDto> reasoningSteps,
        @JsonProperty("classical_remedies") @JsonAlias("classicalRemedies") List<String> classicalRemedies
) {
    public ReasoningSynthesisResponseDto withIds(UUID analysisSessionId, UUID birthProfileId) {
        return new ReasoningSynthesisResponseDto(
                analysisSessionId,
                birthProfileId,
                this.frameworkVersion(),
                this.questionText(),
                this.questionCategory(),
                this.primaryHouses(),
                this.overallVerdict(),
                this.compositeScore(),
                this.reasoningSteps(),
                this.classicalRemedies()
        );
    }
}
