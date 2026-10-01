package com.astroai.ai.tool;

import java.util.Map;

public interface AstrologyTool {

    String getName();

    String getDescription();

    Map<String, Object> getParameterSchema();

    Map<String, Object> execute(Map<String, Object> arguments);
}
