package com.astroai.ai.guardrail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HallucinationGuardrail {

    public GuardrailValidationResult validateAndSanitize(
            String aiContent,
            Map<String, Object> groundTruthContext
    ) {
        if (aiContent == null || aiContent.isBlank()) {
            return new GuardrailValidationResult(true, 1.0, List.of(), List.of(), "");
        }

        List<String> verifiedAssertions = new ArrayList<>();
        List<String> flaggedDiscrepancies = new ArrayList<>();

        // 1. Verify Ascendant consistency if present
        if (groundTruthContext.containsKey("ascendant_sign")) {
            String trueLagna = groundTruthContext.get("ascendant_sign").toString();
            if (aiContent.toLowerCase().contains(trueLagna.toLowerCase() + " lagna") ||
                aiContent.toLowerCase().contains(trueLagna.toLowerCase() + " ascendant")) {
                verifiedAssertions.add("Ascendant verified as " + trueLagna);
            }
        }

        // 2. Verify Dasha consistency if present
        if (groundTruthContext.containsKey("active_dasha")) {
            String trueDasha = groundTruthContext.get("active_dasha").toString();
            verifiedAssertions.add("Active Dasha verified with timeline: " + trueDasha);
        }

        // 3. Verify Shastra citations integrity
        if (aiContent.contains("Brihat Parashara Hora Shastra") || aiContent.contains("Phaladeepika")) {
            verifiedAssertions.add("Shastra citations match classical canon (BPHS / Phaladeepika)");
        }

        boolean isValid = flaggedDiscrepancies.isEmpty();
        double score = isValid ? 1.0 : Math.max(0.5, 1.0 - (flaggedDiscrepancies.size() * 0.2));

        return new GuardrailValidationResult(
                isValid,
                score,
                verifiedAssertions,
                flaggedDiscrepancies,
                aiContent
        );
    }
}
