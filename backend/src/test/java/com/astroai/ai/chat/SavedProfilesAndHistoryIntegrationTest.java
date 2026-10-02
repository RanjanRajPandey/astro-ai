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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SavedProfilesAndHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void verifiesSavedProfilesManagementAndConsultationHistoryExportAndSearch() throws Exception {
        // 1. Create a user via auth
        String userEmail = "history_native_" + System.currentTimeMillis() + "@astroai.com";
        String regJson = String.format("""
                {
                  "email": "%s",
                  "password": "Password123!",
                  "full_name": "Rohan Deshmukh",
                  "role": "USER"
                }
                """, userEmail);

        MvcResult regRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regJson))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(regRes.getResponse().getContentAsString())
                .path("data").path("user").path("id").asText();

        // 2. Create two birth profiles for this user
        String p1Json = String.format("""
                {
                  "userId": "%s",
                  "name": "Rohan Self",
                  "dateOfBirth": "1991-08-14",
                  "timeOfBirth": "08:15:00",
                  "placeOfBirth": "Pune",
                  "gender": "MALE"
                }
                """, userId);

        MvcResult p1Res = mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(p1Json))
                .andExpect(status().isCreated())
                .andReturn();

        String p1Id = objectMapper.readTree(p1Res.getResponse().getContentAsString())
                .path("data").path("id").asText();

        String p2Json = String.format("""
                {
                  "userId": "%s",
                  "name": "Ananya Spouse",
                  "dateOfBirth": "1993-02-22",
                  "timeOfBirth": "14:45:00",
                  "placeOfBirth": "Mumbai",
                  "gender": "FEMALE"
                }
                """, userId);

        mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(p2Json))
                .andExpect(status().isCreated());

        // 3. List profiles for user
        mockMvc.perform(get("/api/birth-profiles?userId=" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        // 4. Update profile 1
        String updateP1Json = String.format("""
                {
                  "userId": "%s",
                  "name": "Rohan Deshmukh (Corrected Time)",
                  "dateOfBirth": "1991-08-14",
                  "timeOfBirth": "08:20:00",
                  "placeOfBirth": "Pune",
                  "gender": "MALE"
                }
                """, userId);

        mockMvc.perform(put("/api/birth-profiles/" + p1Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateP1Json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Rohan Deshmukh (Corrected Time)"))
                .andExpect(jsonPath("$.data.timeOfBirth").value("08:20:00"));

        // 5. Create Chat Session for p1
        String createSessionJson = String.format("""
                {
                  "birth_profile_id": "%s",
                  "user_id": "%s",
                  "title": "Wealth & Business Partnerships Inquiry"
                }
                """, p1Id, userId);

        MvcResult sessionRes = mockMvc.perform(post("/api/ai/chat-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createSessionJson))
                .andExpect(status().isCreated())
                .andReturn();

        String sessionId = objectMapper.readTree(sessionRes.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 6. Send Consultation Message
        String sendMsgJson = """
                {
                  "message": "What business partnership yogas are formed by my 7th and 10th lords?",
                  "domain_category": "CAREER_AND_PROFESSION",
                  "include_reasoning": true
                }
                """;

        mockMvc.perform(post("/api/ai/chat-sessions/" + sessionId + "/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sendMsgJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sender_role").value("ASSISTANT"));

        // 7. Get user's sessions
        mockMvc.perform(get("/api/ai/chat-sessions/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(sessionId));

        // 8. Search sessions by keyword
        mockMvc.perform(get("/api/ai/chat-sessions/search?userId=" + userId + "&query=Partnerships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        // 9. Export consultation as Markdown
        MvcResult exportRes = mockMvc.perform(get("/api/ai/chat-sessions/" + sessionId + "/export"))
                .andExpect(status().isOk())
                .andReturn();

        String markdown = exportRes.getResponse().getContentAsString();
        assertThat(markdown).contains("# Vedic Astrology Consultation Report");
        assertThat(markdown).contains("Rohan Deshmukh (Corrected Time)");
        assertThat(markdown).contains("AstroAI Shastric Synthesis");
        assertThat(markdown).contains("Brihat Parashara Hora Shastra");

        // 10. Delete session
        mockMvc.perform(delete("/api/ai/chat-sessions/" + sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(chatSessionRepository.findById(UUID.fromString(sessionId))).isEmpty();
        assertThat(chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(UUID.fromString(sessionId))).isEmpty();

        // 11. Delete profile
        mockMvc.perform(delete("/api/birth-profiles/" + p1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
