package com.astroai.ai.tool;

import com.astroai.divisional.DivisionalCalculationResponseDto;
import com.astroai.divisional.DivisionalChartService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetDivisionalChartTool implements AstrologyTool {

    private final DivisionalChartService divisionalChartService;
    private final ObjectMapper objectMapper;

    public GetDivisionalChartTool(DivisionalChartService divisionalChartService, ObjectMapper objectMapper) {
        this.divisionalChartService = divisionalChartService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_divisional_chart";
    }

    @Override
    public String getDescription() {
        return "Fetches 16 Shodashavarga divisional charts (D9 Navamsha, D10 Dashamsha, D2, D7, etc.).";
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
        DivisionalCalculationResponseDto res = divisionalChartService.calculateAndPersistAllShodashavarga(profileId);
        return objectMapper.convertValue(res, new TypeReference<>() {});
    }
}
