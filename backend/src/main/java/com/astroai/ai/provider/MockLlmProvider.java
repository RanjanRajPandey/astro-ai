package com.astroai.ai.provider;

import org.springframework.stereotype.Component;

@Component
public class MockLlmProvider implements LlmProvider {

    @Override
    public String getProviderName() {
        return "MOCK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public LlmChatResponse generateResponse(LlmChatRequest request) {
        String msg = request.userMessage();
        String sys = request.systemInstruction() != null ? request.systemInstruction() : "";

        StringBuilder sb = new StringBuilder();
        sb.append("### Vedic Astrology Consultation & Deterministic Synthesis\n\n");
        sb.append("**Namaste.** In accordance with the timeless principles of *Brihat Parashara Hora Shastra* ");
        sb.append("and *Phaladeepika*, here is the traceable astrological analysis for your inquiry: *\"").append(msg).append("\"*.\n\n");

        if (sys.contains("Reasoning Steps:") || sys.contains("composite_score")) {
            sb.append("#### 1. Core Synthesis & Shastric Verdict\n");
            sb.append("Our deterministic reasoning engine has synthesized your natal potential, harmonic divisional charts, ");
            sb.append("active classical yogas, and temporal transit triggers. The astrological indicators show a coherent karmic pattern ");
            sb.append("governed by the active Vimshottari period.\n\n");

            sb.append("#### 2. Key Astrological Drivers\n");
            sb.append("- **Natal Foundation (D1 Rashi & Bhava Bala):** The primary house and its lord determine the capacity to manifest results in this domain.\n");
            sb.append("- **Divisional Corroboration:** Micro-potency confirmed in the subtle harmonic vargas according to Parashari principles.\n");
            sb.append("- **Classical Yogas:** Auspicious combinations act as karmic accelerators, while afflictions require mindful forbearance.\n");
            sb.append("- **Temporal Window (Gochar & Dasha):** The double transit of Jupiter and Saturn activates the karmic readiness of the native.\n\n");

            sb.append("#### 3. Prescribed Shastric Remedies (Parihara)\n");
            sb.append("To propitiate the planetary energies and align with your cosmic path:\n");
            sb.append("1. **Daily Sadhana:** Recite the Gayatri Mantra or Maha Mrityunjaya Mantra at sunrise for mental peace and spiritual alignment.\n");
            sb.append("2. **Dana (Charity):** Perform Annadana (food donation) or assist elders on auspicious weekdays.\n");
            sb.append("3. **Conscious Action:** Channel planetary tendencies through righteous conduct (Satya and Dharma).\n\n");
        } else {
            sb.append("Based on your planetary positions and current dasha alignments, your chart reflects steady karmic progression. ");
            sb.append("All calculations, house placements, and yogas are verified deterministically through the Swiss Ephemeris engine.\n\n");
        }

        sb.append("> **Why This Answer?** Every observation in this consultation is backed by deterministic planetary coordinates ");
        sb.append("and classical shastra citations (BPHS Ch. 27–28, Phaladeepika Ch. 19 & 26). The AI does not guess or calculate astronomical figures.");

        return LlmChatResponse.ofText(sb.toString(), "MOCK", "astro-ai-deterministic-v1");
    }
}
