package com.astroai.ai.tool;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AstrologyToolRegistry {

    private final Map<String, AstrologyTool> toolMap = new LinkedHashMap<>();

    public AstrologyToolRegistry(List<AstrologyTool> tools) {
        if (tools != null) {
            for (AstrologyTool tool : tools) {
                toolMap.put(tool.getName(), tool);
            }
        }
    }

    public List<AstrologyTool> getAllTools() {
        return List.copyOf(toolMap.values());
    }

    public Optional<AstrologyTool> getTool(String name) {
        return Optional.ofNullable(toolMap.get(name));
    }

    public Map<String, Object> executeTool(String toolName, Map<String, Object> arguments) {
        AstrologyTool tool = toolMap.get(toolName);
        if (tool == null) {
            throw new IllegalArgumentException("Unknown deterministic astrology tool: " + toolName);
        }
        return tool.execute(arguments != null ? arguments : Collections.emptyMap());
    }
}
