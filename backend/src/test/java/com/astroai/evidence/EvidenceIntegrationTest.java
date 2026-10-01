package com.astroai.evidence;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class EvidenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalysisSessionRepository analysisSessionRepository;

    @Autowired
    private EvidenceItemRepository evidenceItemRepository;

    @Test
    void generatesAndPersistsEvidenceChainWithTraceableShastras() throws Exception {
        String createProfileJson = """
                {
                  "name": "Evidence Test Native",
                  "dateOfBirth": "1992-07-24",
                  "timeOfBirth": "11:15:00",
                  "placeOfBirth": "New Delhi",
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

        String generatePayload = String.format("""
                {
                  "birth_profile_id": "%s",
                  "question_text": "Will I achieve a high-level leadership promotion in my career?",
                  "question_category": "CAREER_AND_PROFESSION"
                }
                """, profileId);

        MvcResult genResult = mockMvc.perform(post("/api/evidence/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(generatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.birth_profile_id").value(profileId))
                .andExpect(jsonPath("$.data.question_category").value("CAREER_AND_PROFESSION"))
                .andExpect(jsonPath("$.data.total_evidence_count").isNumber())
                .andExpect(jsonPath("$.data.evidence_items").isArray())
                .andExpect(jsonPath("$.data.evidence_items[0].rule_reference").isNotEmpty())
                .andReturn();

        String sessionId = objectMapper.readTree(genResult.getResponse().getContentAsString())
                .path("data").path("analysis_session_id").asText();

        assertThat(sessionId).isNotEmpty();
        assertThat(analysisSessionRepository.findById(java.util.UUID.fromString(sessionId))).isPresent();
        assertThat(evidenceItemRepository.findByAnalysisSessionId(java.util.UUID.fromString(sessionId))).isNotEmpty();

        // Verify GET latest endpoint
        mockMvc.perform(get("/api/evidence/latest/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysis_session_id").value(sessionId))
                .andExpect(jsonPath("$.data.question_category").value("CAREER_AND_PROFESSION"))
                .andExpect(jsonPath("$.data.evidence_items").isNotEmpty());
    }
}
