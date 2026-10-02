package com.astroai.ai.guardrail;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AiEvaluationBenchmarkTest {

    private HallucinationGuardrail guardrail;

    @BeforeEach
    void setUp() {
        guardrail = new HallucinationGuardrail();
    }

    @Test
    @DisplayName("Verify Ground Truth: Ascendant and Classical Citations Validated")
    void testValidAiConsultationPassesGuardrail() {
        Map<String, Object> groundTruth = new HashMap<>();
        groundTruth.put("ascendant_sign", "Taurus");
        groundTruth.put("active_dasha", "Saturn-Jupiter");

        String aiResponse = """
                Based on your Taurus Lagna and the active Saturn-Jupiter Vimshottari dasha period,
                your 10th house is activated for career endeavors. As noted in Brihat Parashara Hora Shastra,
                benefic aspects provide stability. Classical remedies include regular meditation and Dana.
                """;

        GuardrailValidationResult result = guardrail.validateAndSanitize(aiResponse, groundTruth);

        assertTrue(result.isValid());
        assertEquals(1.0, result.confidenceScore(), 0.001);
        assertTrue(result.flaggedDiscrepancies().isEmpty());
        assertFalse(result.verifiedAssertions().isEmpty());
        assertTrue(result.verifiedAssertions().stream().anyMatch(a -> a.contains("Taurus")));
        assertTrue(result.verifiedAssertions().stream().anyMatch(a -> a.contains("Saturn-Jupiter")));
        assertTrue(result.verifiedAssertions().stream().anyMatch(a -> a.contains("BPHS")));
    }

    @Test
    @DisplayName("Catch Hallucinated Ascendant: AI Claims Aries Lagna when Ground Truth is Taurus")
    void testCatchesHallucinatedAscendant() {
        Map<String, Object> groundTruth = new HashMap<>();
        groundTruth.put("ascendant_sign", "Taurus");

        String hallucinatedResponse = """
                Looking at your chart with an Aries ascendant, your Mars is the ruler of your first house.
                """;

        GuardrailValidationResult result = guardrail.validateAndSanitize(hallucinatedResponse, groundTruth);

        assertFalse(result.isValid());
        assertTrue(result.confidenceScore() < 1.0);
        assertEquals(1, result.flaggedDiscrepancies().size());
        assertTrue(result.flaggedDiscrepancies().get(0).contains("Hallucination detected"));
        assertTrue(result.flaggedDiscrepancies().get(0).contains("aries"));
    }

    @Test
    @DisplayName("Catch Ethics Violation: Unhedged Fatalistic Doom Prediction Flagged")
    void testCatchesFatalisticPrediction() {
        Map<String, Object> groundTruth = new HashMap<>();
        groundTruth.put("ascendant_sign", "Virgo");

        String fatalisticResponse = """
                In your Virgo lagna, 8th house Mars indicates you will die in a severe accident next year.
                """;

        GuardrailValidationResult result = guardrail.validateAndSanitize(fatalisticResponse, groundTruth);

        assertFalse(result.isValid());
        assertTrue(result.flaggedDiscrepancies().stream()
                .anyMatch(d -> d.contains("Ethics guardrail violation")));
    }

    @Test
    @DisplayName("Empty or Blank AI Output Handled Gracefully")
    void testBlankAiOutputHandledGracefully() {
        Map<String, Object> groundTruth = Map.of("ascendant_sign", "Cancer");
        GuardrailValidationResult result = guardrail.validateAndSanitize("", groundTruth);
        assertTrue(result.isValid());
        assertEquals(1.0, result.confidenceScore());
    }
}
