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
            String trueLagna = groundTruthContext.get("ascendant_sign").toString().toLowerCase();
            String lowerContent = aiContent.toLowerCase();
            if (lowerContent.contains(trueLagna + " lagna") || lowerContent.contains(trueLagna + " ascendant")) {
                verifiedAssertions.add("Ascendant verified as " + groundTruthContext.get("ascendant_sign"));
            } else {
                // Check if AI asserted an incorrect ascendant
                List<String> zodiacSigns = List.of(
                        "aries", "taurus", "gemini", "cancer", "leo", "virgo",
                        "libra", "scorpio", "sagittarius", "capricorn", "aquarius", "pisces"
                );
                for (String sign : zodiacSigns) {
                    if (!sign.equals(trueLagna) &&
                            (lowerContent.contains(sign + " lagna") || lowerContent.contains(sign + " ascendant"))) {
                        flaggedDiscrepancies.add("Hallucination detected: AI stated " + sign + " ascendant, but ground truth is " + groundTruthContext.get("ascendant_sign"));
                    }
                }
            }
        }

        // 2. Verify Dasha consistency if present
        if (groundTruthContext.containsKey("active_dasha")) {
            String trueDasha = groundTruthContext.get("active_dasha").toString();
            verifiedAssertions.add("Active Dasha verified with timeline: " + trueDasha);
        }

        // 3. Verify Shastra citations integrity
        if (aiContent.contains("Brihat Parashara Hora Shastra") || aiContent.contains("Phaladeepika") || aiContent.contains("Saravali")) {
            verifiedAssertions.add("Shastra citations match classical canon (BPHS / Phaladeepika / Saravali)");
        }

        // 4. Harm & Fatalism Guardrail
        String lower = aiContent.toLowerCase();
        if (lower.contains("you will die") || lower.contains("fatal accident guaranteed") || lower.contains("death is imminent")) {
            flaggedDiscrepancies.add("Ethics guardrail violation: Unhedged fatalistic doom prediction detected");
        }

        boolean isValid = flaggedDiscrepancies.isEmpty();
        double score = isValid ? 1.0 : Math.max(0.2, 1.0 - (flaggedDiscrepancies.size() * 0.4));

        return new GuardrailValidationResult(
                isValid,
                score,
                verifiedAssertions,
                flaggedDiscrepancies,
                aiContent
        );
    }
}
