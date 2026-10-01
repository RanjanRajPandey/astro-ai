package com.astroai.ai.tool;

import com.astroai.dasha.DashaCalculationResponseDto;
import com.astroai.dasha.DashaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetVimshottariDashaTool implements AstrologyTool {

    private final DashaService dashaService;
    private final ObjectMapper objectMapper;

    public GetVimshottariDashaTool(DashaService dashaService, ObjectMapper objectMapper) {
        this.dashaService = dashaService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_vimshottari_dasha";
    }

    @Override
    public String getDescription() {
        return "Fetches 5-level Vimshottari Dasha timeline (MD, AD, PD) and currently running dasha periods.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", Map.of(
                "birth_profile_id", Map.of("type", "string", "description", "UUID of the birth profile"),
                "target_date_iso", Map.of("type", "string", "description", "Optional target ISO datetime UTC")
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
        String targetDate = (String) arguments.get("target_date_iso");
        DashaCalculationResponseDto res = dashaService.calculateAndPersistDashas(profileId, targetDate);
        return objectMapper.convertValue(res, new TypeReference<>() {});
    }
}
