package com.astroai.ai.tool;

import com.astroai.strength.BhavaBalaCalculationResponseDto;
import com.astroai.strength.BhavaBalaService;
import com.astroai.strength.ShadbalaCalculationResponseDto;
import com.astroai.strength.ShadbalaService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GetShadbalaAndBhavaBalaTool implements AstrologyTool {

    private final ShadbalaService shadbalaService;
    private final BhavaBalaService bhavaBalaService;
    private final ObjectMapper objectMapper;

    public GetShadbalaAndBhavaBalaTool(
            ShadbalaService shadbalaService,
            BhavaBalaService bhavaBalaService,
            ObjectMapper objectMapper
    ) {
        this.shadbalaService = shadbalaService;
        this.bhavaBalaService = bhavaBalaService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "get_shadbala_and_bhavabala";
    }

    @Override
    public String getDescription() {
        return "Fetches 6-fold planetary Shadbala, 16-varga Vimshopaka Bala, and 12 house Bhava Bala strengths.";
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
        ShadbalaCalculationResponseDto shadbala = shadbalaService.calculateAndPersistShadbala(profileId);
        BhavaBalaCalculationResponseDto bhavaBala = bhavaBalaService.calculateAndPersistBhavaBala(profileId);

        Map<String, Object> combined = new LinkedHashMap<>();
        combined.put("shadbala", objectMapper.convertValue(shadbala, new TypeReference<Map<String, Object>>() {}));
        combined.put("bhava_bala", objectMapper.convertValue(bhavaBala, new TypeReference<Map<String, Object>>() {}));
        return combined;
    }
}
