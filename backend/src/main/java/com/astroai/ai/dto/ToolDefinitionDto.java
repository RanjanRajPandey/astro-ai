package com.astroai.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ToolDefinitionDto(
        @JsonProperty("name") String name,
        @JsonProperty("description") String description,
        @JsonProperty("parameter_schema") Map<String, Object> parameterSchema
) {}
