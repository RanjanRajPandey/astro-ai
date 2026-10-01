package com.astroai.ai.guardrail;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GuardrailValidationResult(
        @JsonProperty("is_valid") @JsonAlias("isValid") boolean isValid,
        @JsonProperty("confidence_score") @JsonAlias("confidenceScore") double confidenceScore,
        @JsonProperty("verified_assertions") @JsonAlias("verifiedAssertions") List<String> verifiedAssertions,
        @JsonProperty("flagged_discrepancies") @JsonAlias("flaggedDiscrepancies") List<String> flaggedDiscrepancies,
        @JsonProperty("audited_content") @JsonAlias("auditedContent") String auditedContent
) {
    public static GuardrailValidationResult valid(String content, List<String> verified) {
        return new GuardrailValidationResult(true, 1.0, verified, List.of(), content);
    }
}
