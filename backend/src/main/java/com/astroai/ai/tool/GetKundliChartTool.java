package com.astroai.ai.tool;

import com.astroai.chart.ChartService;
import com.astroai.chart.KundliChartResponseDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetKundliChartTool implements AstrologyTool {

    private final ChartService chartService;
    private final ObjectMapper objectMapper;

    public GetKundliChartTool(ChartService chartService, ObjectMapper objectMapper) {
        this.chartService = chartService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_kundli_chart";
    }

    @Override
    public String getDescription() {
        return "Fetches deterministic D1 Natal Rashi chart, planetary longitudes, signs, houses, and ascendant.";
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
        KundliChartResponseDto chart = chartService.getD1KundliChart(profileId);
        return objectMapper.convertValue(chart, new TypeReference<>() {});
    }
}
