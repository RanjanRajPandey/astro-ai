package com.astroai.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ToolExecutionRequestDto(
        @JsonProperty("tool_name") String toolName,
        @JsonProperty("arguments") Map<String, Object> arguments
) {}
