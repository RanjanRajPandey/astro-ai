package com.astroai.ai.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
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
class AiChatSessionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void verifiesMultiTurnChatSessionAndDeepExplainabilityTrace() throws Exception {
        // 1. Create a test birth profile
        String createProfileJson = """
                {
                  "name": "Phase 19 Chat Native",
                  "dateOfBirth": "1993-04-18",
                  "timeOfBirth": "10:30:00",
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

        // 2. Create Chat Session
        String createSessionJson = String.format("""
                {
                  "birth_profile_id": "%s",
                  "title": "Career Leadership & Dharma Consultation"
                }
                """, profileId);

        MvcResult sessionResult = mockMvc.perform(post("/api/ai/chat-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createSessionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.birth_profile_id").value(profileId))
                .andExpect(jsonPath("$.data.title").value("Career Leadership & Dharma Consultation"))
                .andReturn();

        String sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        assertThat(sessionId).isNotEmpty();
        assertThat(chatSessionRepository.findById(UUID.fromString(sessionId))).isPresent();

        // 3. Send Consultation Message
        String sendMessageJson = """
                {
                  "message": "How does the current transit of Jupiter interact with my 10th house lord for career progression?",
                  "domain_category": "CAREER_AND_PROFESSION",
                  "include_reasoning": true
                }
                """;

        MvcResult msgResult = mockMvc.perform(post("/api/ai/chat-sessions/" + sessionId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendMessageJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sender_role").value("ASSISTANT"))
                .andExpect(jsonPath("$.data.message_content").isNotEmpty())
                .andExpect(jsonPath("$.data.explainability_trace.shastric_citations").isArray())
                .andExpect(jsonPath("$.data.explainability_trace.guardrail_result.is_valid").value(true))
                .andReturn();

        String messageId = objectMapper.readTree(msgResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 4. Retrieve conversation history
        mockMvc.perform(get("/api/ai/chat-sessions/" + sessionId + "/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].sender_role").value("USER"))
                .andExpect(jsonPath("$.data[1].sender_role").value("ASSISTANT"));

        // 5. Query "Why This Answer?" explainability endpoint
        MvcResult explainResult = mockMvc.perform(get("/api/ai/chat-sessions/messages/" + messageId + "/explain"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message_id").value(messageId))
                .andExpect(jsonPath("$.data.divisional_charts_consulted").isArray())
                .andExpect(jsonPath("$.data.ground_truth_context.ascendant_sign").isNotEmpty())
                .andExpect(jsonPath("$.data.classical_remedies").isArray())
                .andReturn();

        String traceJson = explainResult.getResponse().getContentAsString();
        assertThat(traceJson).contains("Brihat Parashara Hora Shastra");
    }
}
