package com.astroai.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiToolLayerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void verifiesToolRegistryListingAndDeterministicExecution() throws Exception {
        // 1. Verify GET /api/ai/tools lists all deterministic astrology tools
        mockMvc.perform(get("/api/ai/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.name == 'get_kundli_chart')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'get_vimshottari_dasha')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'get_evidence_chain')]").exists())
                .andExpect(jsonPath("$.data[?(@.name == 'get_reasoning_synthesis')]").exists());

        // 2. Create a test birth profile
        String createProfileJson = """
                {
                  "name": "AI Tool Native",
                  "dateOfBirth": "1990-05-15",
                  "timeOfBirth": "14:20:00",
                  "placeOfBirth": "New Delhi",
                  "gender": "MALE"
                }
                """;

        MvcResult profileResult = mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProfileJson))
                .andExpect(status().isCreated())
                .andReturn();

        String profileId = objectMapper.readTree(profileResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 3. Test executing get_kundli_chart tool directly
        String toolExecJson = String.format("""
                {
                  "tool_name": "get_kundli_chart",
                  "arguments": {
                    "birth_profile_id": "%s"
                  }
                }
                """, profileId);

        mockMvc.perform(post("/api/ai/tools/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolExecJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tool_name").value("get_kundli_chart"))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.result.vargaCode").value("D1"));
    }

    @Test
    void generatesAiConsultationWithGroundTruthAndGuardrails() throws Exception {
        // 1. Create a test birth profile
        String createProfileJson = """
                {
                  "name": "AI Consultation Native",
                  "dateOfBirth": "1995-12-08",
                  "timeOfBirth": "07:45:00",
                  "placeOfBirth": "Varanasi",
                  "gender": "FEMALE"
                }
                """;

        MvcResult profileResult = mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createProfileJson))
                .andExpect(status().isCreated())
                .andReturn();

        String profileId = objectMapper.readTree(profileResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. Chat with AI consultant
        String chatJson = String.format("""
                {
                  "birth_profile_id": "%s",
                  "user_message": "What does my chart indicate regarding higher education and overseas travel?",
                  "domain_category": "EDUCATION_AND_INTELLECT",
                  "include_reasoning": true
                }
                """, profileId);

        MvcResult chatResult = mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(chatJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.response").isNotEmpty())
                .andExpect(jsonPath("$.data.provider").value("MOCK"))
                .andExpect(jsonPath("$.data.guardrail_result.is_valid").value(true))
                .andExpect(jsonPath("$.data.guardrail_result.confidence_score").isNumber())
                .andExpect(jsonPath("$.data.ground_truth_context.birth_profile_id").value(profileId))
                .andExpect(jsonPath("$.data.ground_truth_context.ascendant_sign").isNotEmpty())
                .andReturn();

        String aiResponse = objectMapper.readTree(chatResult.getResponse().getContentAsString())
                .path("data").path("response").asText();

        assertThat(aiResponse).contains("Brihat Parashara Hora Shastra");
    }
}
