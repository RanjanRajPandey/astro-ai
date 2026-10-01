package com.astroai.ai.context;

import com.astroai.birth.BirthProfileResponse;
import com.astroai.birth.BirthProfileService;
import com.astroai.chart.KundliChartResponseDto;
import com.astroai.chart.ChartService;
import com.astroai.dasha.DashaCalculationResponseDto;
import com.astroai.dasha.DashaService;
import com.astroai.evidence.EvidenceService;
import com.astroai.reasoning.GenerateReasoningRequestDto;
import com.astroai.reasoning.ReasoningService;
import com.astroai.reasoning.ReasoningSynthesisResponseDto;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AstroContextManager {

    private static final Logger log = LoggerFactory.getLogger(AstroContextManager.class);

    private final BirthProfileService birthProfileService;
    private final ChartService chartService;
    private final DashaService dashaService;
    private final EvidenceService evidenceService;
    private final ReasoningService reasoningService;

    public AstroContextManager(
            BirthProfileService birthProfileService,
            ChartService chartService,
            DashaService dashaService,
            EvidenceService evidenceService,
            ReasoningService reasoningService
    ) {
        this.birthProfileService = birthProfileService;
        this.chartService = chartService;
        this.dashaService = dashaService;
        this.evidenceService = evidenceService;
        this.reasoningService = reasoningService;
    }

    public AstroContext buildContext(
            UUID birthProfileId,
            String userQuestion,
            String domainCategory,
            boolean includeReasoning
    ) {
        Map<String, Object> groundTruth = new LinkedHashMap<>();
        StringBuilder sys = new StringBuilder();

        sys.append("You are AstroAI, an ethical Vedic Astrology Consultant powered strictly by classical Vedic Jyotish shastras (Brihat Parashara Hora Shastra, Phaladeepika, Jaimini Upadesha Sutras).\n\n");
        sys.append("### CRITICAL ANTI-HALLUCINATION DIRECTIVES:\n");
        sys.append("1. THE LLM IS NEVER THE ASTROLOGY CALCULATION ENGINE. All planetary coordinates, ascendants, houses, dashas, strengths, and yogas are computed deterministically.\n");
        sys.append("2. You must NEVER invent, alter, or hallucinate planetary degrees, signs, houses, or timelines.\n");
        sys.append("3. Ground all interpretations firmly on the verified facts provided below or obtained via deterministic tools.\n");
        sys.append("4. Never deliver fatalistic doom-mongering. Offer balanced, empowering insights and classical shastric remedies (Dana, Japa, Satya, Sewa).\n");
        sys.append("5. Always cite classical shastras (e.g., BPHS, Phaladeepika) and explain \"Why This Answer?\" transparently.\n\n");

        if (birthProfileId != null) {
            try {
                BirthProfileResponse profile = birthProfileService.getProfile(birthProfileId);
                groundTruth.put("birth_profile_id", birthProfileId.toString());
                groundTruth.put("native_name", profile.name());
                groundTruth.put("date_of_birth", profile.dateOfBirth().toString());
                groundTruth.put("time_of_birth", profile.timeOfBirth() != null ? profile.timeOfBirth().toString() : "Not specified");
                groundTruth.put("place_of_birth", profile.placeOfBirth());

                sys.append("### NATIVE BIRTH PARTICULARS:\n");
                sys.append("- Name: ").append(profile.name()).append("\n");
                sys.append("- Birth Date: ").append(profile.dateOfBirth()).append("\n");
                sys.append("- Birth Time: ").append(profile.timeOfBirth() != null ? profile.timeOfBirth() : "N/A").append("\n");
                sys.append("- Place: ").append(profile.placeOfBirth()).append("\n\n");
            } catch (Exception e) {
                log.warn("Could not retrieve birth profile for context: {}", e.getMessage());
            }

            try {
                KundliChartResponseDto chart = chartService.getD1KundliChart(birthProfileId);
                if (chart != null) {
                    String ascSign = chart.ascendant() != null ? chart.ascendant().sign() : "Unknown";
                    groundTruth.put("ascendant_sign", ascSign);
                    groundTruth.put("moon_sign", chart.moonSign());
                    groundTruth.put("moon_nakshatra", chart.moonNakshatra());
                    groundTruth.put("sun_sign", chart.sunSign());
                    groundTruth.put("ayanamsha", chart.ayanamshaType());

                    sys.append("### VERIFIED NATAL CHART (D1 RASHI):\n");
                    sys.append("- Ascendant (Lagna): ").append(ascSign).append("\n");
                    sys.append("- Moon Sign (Rashi): ").append(chart.moonSign()).append(" (Nakshatra: ").append(chart.moonNakshatra()).append(")\n");
                    sys.append("- Sun Sign: ").append(chart.sunSign()).append("\n");
                    sys.append("- Ayanamsha: ").append(chart.ayanamshaType()).append("\n\n");

                    if (chart.planets() != null && !chart.planets().isEmpty()) {
                        sys.append("### PLANETARY PLACEMENTS:\n");
                        chart.planets().forEach(p -> sys.append(String.format("- %s in %s (House %d, %s°, Nakshatra: %s%s)\n",
                                p.planet(), p.sign(), p.house(),
                                p.degreeInSign() != null ? p.degreeInSign().toPlainString() : "0",
                                p.nakshatra(),
                                p.retrograde() ? " [Retrograde]" : "")));
                        sys.append("\n");
                    }
                }
            } catch (Exception e) {
                log.warn("Could not retrieve D1 chart for context: {}", e.getMessage());
            }

            try {
                DashaCalculationResponseDto dashaRes = dashaService.calculateAndPersistDashas(birthProfileId, null);
                if (dashaRes != null && dashaRes.activeStack() != null && !dashaRes.activeStack().isEmpty()) {
                    var current = dashaRes.activeStack().get(0);
                    String activeDashaStr = current.planet() + " " + current.levelName();
                    groundTruth.put("active_dasha", activeDashaStr);
                    sys.append("### ACTIVE VIMSHOTTARI DASHA:\n");
                    sys.append("- Current Period: ").append(activeDashaStr).append("\n\n");
                } else if (dashaRes != null && dashaRes.birthDashaLord() != null) {
                    String activeDashaStr = dashaRes.birthDashaLord() + " Mahadasha";
                    groundTruth.put("active_dasha", activeDashaStr);
                    sys.append("### ACTIVE VIMSHOTTARI DASHA:\n");
                    sys.append("- Birth Dasha Lord: ").append(activeDashaStr).append("\n\n");
                }
            } catch (Exception e) {
                log.warn("Could not retrieve active dasha for context: {}", e.getMessage());
            }

            if (includeReasoning) {
                try {
                    GenerateReasoningRequestDto reasoningReq = new GenerateReasoningRequestDto(
                            birthProfileId,
                            userQuestion != null ? userQuestion : "General Consultation",
                            domainCategory != null ? domainCategory : "GENERAL"
                    );
                    ReasoningSynthesisResponseDto reasoningRes = reasoningService.synthesizeAndPersistReasoning(reasoningReq);
                    if (reasoningRes != null) {
                        groundTruth.put("composite_score", reasoningRes.compositeScore());
                        groundTruth.put("overall_verdict", reasoningRes.overallVerdict());
                        groundTruth.put("classical_remedies", reasoningRes.classicalRemedies());

                        sys.append("### DETERMINISTIC REASONING SYNTHESIS:\n");
                        sys.append("- Overall Shastric Verdict: ").append(reasoningRes.overallVerdict()).append("\n");
                        sys.append(String.format("- Composite Auspiciousness Score: %.2f / 1.00\n", reasoningRes.compositeScore()));
                        if (reasoningRes.primaryHouses() != null && !reasoningRes.primaryHouses().isEmpty()) {
                            sys.append("- Key Houses Involved: ").append(reasoningRes.primaryHouses()).append("\n");
                        }
                        if (reasoningRes.reasoningSteps() != null) {
                            sys.append("- Reasoning Steps:\n");
                            reasoningRes.reasoningSteps().forEach(step ->
                                    sys.append(String.format("  * [%s] (Confidence: %.2f): %s (Citations: %s)\n",
                                            step.title(), step.confidenceScore(), step.narrative(),
                                            step.shastraCitations() != null ? String.join(", ", step.shastraCitations()) : "None")));
                        }
                        if (reasoningRes.classicalRemedies() != null && !reasoningRes.classicalRemedies().isEmpty()) {
                            sys.append("- Prescribed Classical Remedies:\n");
                            reasoningRes.classicalRemedies().forEach(r -> sys.append("  * ").append(r).append("\n"));
                        }
                        sys.append("\n");
                    }
                } catch (Exception e) {
                    log.warn("Could not synthesize reasoning for context: {}", e.getMessage());
                }
            }
        }

        return new AstroContext(sys.toString(), groundTruth, userQuestion);
    }
}
