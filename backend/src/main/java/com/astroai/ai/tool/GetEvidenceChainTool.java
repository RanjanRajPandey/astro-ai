package com.astroai.ai.tool;

import com.astroai.evidence.EvidenceGenerationResponseDto;
import com.astroai.evidence.EvidenceService;
import com.astroai.evidence.GenerateEvidenceRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetEvidenceChainTool implements AstrologyTool {

    private final EvidenceService evidenceService;
    private final ObjectMapper objectMapper;

    public GetEvidenceChainTool(EvidenceService evidenceService, ObjectMapper objectMapper) {
        this.evidenceService = evidenceService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_evidence_chain";
    }

    @Override
    public String getDescription() {
        return "Generates traceable, verified astrological evidence items with classical BPHS citations.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "birth_profile_id", Map.of("type", "string", "description", "UUID of the birth profile"),
                "question_text", Map.of("type", "string", "description", "The consultation question"),
                "question_category", Map.of("type", "string", "description", "Optional domain category code")
        ));
        schema.put("required", List.of("birth_profile_id"));
        return schema;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> arguments) {
        String profileIdStr = (String) arguments.get("birth_profile_id");
        if (profileIdStr == null || profileIdStr.isBlank()) {
            throw new IllegalArgumentException("Parameter 'birth_profile_id' is required");
        }
        UUID profileId = UUID.fromString(profileIdStr);
        String question = (String) arguments.get("question_text");
        String category = (String) arguments.get("question_category");

        GenerateEvidenceRequestDto req = new GenerateEvidenceRequestDto(profileId, question, category);
        EvidenceGenerationResponseDto res = evidenceService.generateAndPersistEvidence(req);
        return objectMapper.convertValue(res, new TypeReference<>() {});
    }
}
