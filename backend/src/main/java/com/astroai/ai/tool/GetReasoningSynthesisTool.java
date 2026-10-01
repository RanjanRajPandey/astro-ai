package com.astroai.ai.tool;

import com.astroai.reasoning.GenerateReasoningRequestDto;
import com.astroai.reasoning.ReasoningService;
import com.astroai.reasoning.ReasoningSynthesisResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetReasoningSynthesisTool implements AstrologyTool {

    private final ReasoningService reasoningService;
    private final ObjectMapper objectMapper;

    public GetReasoningSynthesisTool(ReasoningService reasoningService, ObjectMapper objectMapper) {
        this.reasoningService = reasoningService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_reasoning_synthesis";
    }

    @Override
    public String getDescription() {
        return "Synthesizes deterministic 5-step Reasoning DAG (Natal Promise, Varga, Yogas, Timing, Verdict, and Remedies).";
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

        GenerateReasoningRequestDto req = new GenerateReasoningRequestDto(profileId, question, category);
        ReasoningSynthesisResponseDto res = reasoningService.synthesizeAndPersistReasoning(req);
        return objectMapper.convertValue(res, new TypeReference<>() {});
    }
}
