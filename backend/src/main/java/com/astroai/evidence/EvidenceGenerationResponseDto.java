package com.astroai.evidence;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvidenceGenerationResponseDto(
        @JsonProperty("analysis_session_id") @JsonAlias("analysisSessionId") UUID analysisSessionId,
        @JsonProperty("birth_profile_id") @JsonAlias("birthProfileId") UUID birthProfileId,
        @JsonProperty("framework_version") @JsonAlias("framework_version") String frameworkVersion,
        @JsonProperty("question_text") @JsonAlias("question_text") String questionText,
        @JsonProperty("question_category") @JsonAlias("question_category") String questionCategory,
        @JsonProperty("primary_houses") @JsonAlias("primary_houses") List<Integer> primaryHouses,
        @JsonProperty("required_vargas") @JsonAlias("required_vargas") List<String> requiredVargas,
        @JsonProperty("total_evidence_count") @JsonAlias("total_evidence_count") int totalEvidenceCount,
        @JsonProperty("favorable_count") @JsonAlias("favorable_count") int favorableCount,
        @JsonProperty("challenging_count") @JsonAlias("challenging_count") int challengingCount,
        @JsonProperty("neutral_count") @JsonAlias("neutral_count") int neutralCount,
        @JsonProperty("evidence_items") @JsonAlias("evidence_items") List<EvidenceItemDto> evidenceItems,
        @JsonProperty("factors_considered") @JsonAlias("factors_considered") List<String> factorsConsidered,
        @JsonProperty("time_windows_summary") @JsonAlias("time_windows_summary") List<String> timeWindowsSummary
) {
    public EvidenceGenerationResponseDto withIds(UUID analysisSessionId, UUID birthProfileId) {
        return new EvidenceGenerationResponseDto(
                analysisSessionId,
                birthProfileId,
                this.frameworkVersion(),
                this.questionText(),
                this.questionCategory(),
                this.primaryHouses(),
                this.requiredVargas(),
                this.totalEvidenceCount(),
                this.favorableCount(),
                this.challengingCount(),
                this.neutralCount(),
                this.evidenceItems(),
                this.factorsConsidered(),
                this.timeWindowsSummary()
        );
    }
}
