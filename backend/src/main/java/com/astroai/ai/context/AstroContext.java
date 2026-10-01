package com.astroai.ai.context;

import java.util.Collections;
import java.util.Map;

public record AstroContext(
        String systemInstruction,
        Map<String, Object> groundTruthContext,
        String formattedUserPrompt
) {
    public AstroContext {
        if (groundTruthContext == null) {
            groundTruthContext = Collections.emptyMap();
        }
    }
}
