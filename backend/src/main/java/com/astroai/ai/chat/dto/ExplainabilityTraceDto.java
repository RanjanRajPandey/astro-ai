package com.astroai.ai.chat.dto;

import com.astroai.ai.guardrail.GuardrailValidationResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ExplainabilityTraceDto(
        @JsonProperty("message_id") UUID messageId,
        @JsonProperty("chat_session_id") UUID chatSessionId,
        @JsonProperty("birth_profile_id") UUID birthProfileId,
        @JsonProperty("ground_truth_context") Map<String, Object> groundTruthContext,
        @JsonProperty("divisional_charts_consulted") List<String> divisionalChartsConsulted,
        @JsonProperty("active_dasha_period") String activeDashaPeriod,
        @JsonProperty("reasoning_verdict") String reasoningVerdict,
        @JsonProperty("composite_score") double compositeScore,
        @JsonProperty("shastric_citations") List<String> shastricCitations,
        @JsonProperty("classical_remedies") List<String> classicalRemedies,
        @JsonProperty("guardrail_result") GuardrailValidationResult guardrailResult,
        @JsonProperty("provider") String provider,
        @JsonProperty("model") String model
) {
    public ExplainabilityTraceDto {
        if (groundTruthContext == null) groundTruthContext = Collections.emptyMap();
        if (divisionalChartsConsulted == null) divisionalChartsConsulted = Collections.emptyList();
        if (shastricCitations == null) shastricCitations = Collections.emptyList();
        if (classicalRemedies == null) classicalRemedies = Collections.emptyList();
    }
}
