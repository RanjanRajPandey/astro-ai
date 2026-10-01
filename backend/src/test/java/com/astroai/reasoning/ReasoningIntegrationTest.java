package com.astroai.reasoning;

import com.astroai.evidence.AnalysisSessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReasoningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalysisSessionRepository analysisSessionRepository;

    @Autowired
    private ReasoningItemRepository reasoningItemRepository;

    @Test
    void synthesizesAndPersistsDeterministicReasoningDag() throws Exception {
        String createProfileJson = """
                {
                  "name": "Reasoning Test Native",
                  "dateOfBirth": "1994-11-20",
                  "timeOfBirth": "09:30:00",
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

        String synthPayload = String.format("""
                {
                  "birth_profile_id": "%s",
                  "question_text": "What are my wealth accumulations and financial stability?",
                  "question_category": "WEALTH_AND_FINANCE"
                }
                """, profileId);

        MvcResult synthResult = mockMvc.perform(post("/api/reasoning/synthesize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(synthPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.birth_profile_id").value(profileId))
                .andExpect(jsonPath("$.data.question_category").value("WEALTH_AND_FINANCE"))
                .andExpect(jsonPath("$.data.reasoning_steps.length()").value(5))
                .andExpect(jsonPath("$.data.composite_score").isNumber())
                .andExpect(jsonPath("$.data.overall_verdict").isNotEmpty())
                .andExpect(jsonPath("$.data.classical_remedies").isNotEmpty())
                .andReturn();

        String sessionId = objectMapper.readTree(synthResult.getResponse().getContentAsString())
                .path("data").path("analysis_session_id").asText();

        assertThat(sessionId).isNotEmpty();
        assertThat(analysisSessionRepository.findById(UUID.fromString(sessionId))).isPresent();
        assertThat(reasoningItemRepository.findByAnalysisSessionIdOrderByStepOrderAsc(UUID.fromString(sessionId)))
                .hasSize(5);

        // Verify GET latest endpoint
        mockMvc.perform(get("/api/reasoning/latest/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysis_session_id").value(sessionId))
                .andExpect(jsonPath("$.data.reasoning_steps.length()").value(5));
    }
}
