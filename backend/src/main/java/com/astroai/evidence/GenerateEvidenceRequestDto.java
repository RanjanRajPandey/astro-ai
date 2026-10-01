package com.astroai.evidence;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateEvidenceRequestDto(
        @NotNull @JsonProperty("birth_profile_id") @JsonAlias("birthProfileId") UUID birthProfileId,
        @JsonProperty("question_text") @JsonAlias("questionText") String questionText,
        @JsonProperty("question_category") @JsonAlias("questionCategory") String questionCategory
) {
}
