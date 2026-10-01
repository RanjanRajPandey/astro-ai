package com.astroai.ai.tool;

import com.astroai.transit.TransitCalculationResponseDto;
import com.astroai.transit.TransitService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetGocharTransitsTool implements AstrologyTool {

    private final TransitService transitService;
    private final ObjectMapper objectMapper;

    public GetGocharTransitsTool(TransitService transitService, ObjectMapper objectMapper) {
        this.transitService = transitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_gochar_transits";
    }

    @Override
    public String getDescription() {
        return "Calculates current planetary transits (Gochara), Vedha obstructions, Sade Sati, and Jupiter-Saturn Double Transit.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "birth_profile_id", Map.of("type", "string", "description", "UUID of the birth profile"),
                "transit_time_iso", Map.of("type", "string", "description", "Optional target ISO datetime UTC")
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
        String transitTime = (String) arguments.get("transit_time_iso");
        TransitCalculationResponseDto res = transitService.calculateAndPersistTransits(profileId, transitTime);
        return objectMapper.convertValue(res, new TypeReference<>() {});
    }
}
