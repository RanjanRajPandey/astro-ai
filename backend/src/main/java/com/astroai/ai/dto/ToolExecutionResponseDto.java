package com.astroai.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ToolExecutionResponseDto(
        @JsonProperty("tool_name") String toolName,
        @JsonProperty("success") boolean success,
        @JsonProperty("result") Map<String, Object> result,
        @JsonProperty("error") String error
) {
    public static ToolExecutionResponseDto ok(String toolName, Map<String, Object> result) {
        return new ToolExecutionResponseDto(toolName, true, result, null);
    }

    public static ToolExecutionResponseDto fail(String toolName, String error) {
        return new ToolExecutionResponseDto(toolName, false, null, error);
    }
}
