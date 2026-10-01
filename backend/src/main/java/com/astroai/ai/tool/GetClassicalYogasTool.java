package com.astroai.ai.tool;

import com.astroai.yoga.YogaCalculationResponseDto;
import com.astroai.yoga.YogaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetClassicalYogasTool implements AstrologyTool {

    private final YogaService yogaService;
    private final ObjectMapper objectMapper;

    public GetClassicalYogasTool(YogaService yogaService, ObjectMapper objectMapper) {
        this.yogaService = yogaService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_classical_yogas";
    }

    @Override
    public String getDescription() {
        return "Evaluates 26 classical Vedic yogas and doshas (Pancha Mahapurusha, Raja, Dhana, Viparita, Sade Sati).";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "birth_profile_id", Map.of("type", "string", "description", "UUID of the birth profile")
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
        YogaCalculationResponseDto res = yogaService.calculateAndPersistYogas(profileId);
        return objectMapper.convertValue(res, new TypeReference<>() {});
    }
}
