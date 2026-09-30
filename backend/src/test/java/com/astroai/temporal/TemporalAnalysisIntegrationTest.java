package com.astroai.temporal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TemporalAnalysisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void calculatesMultiWindowTemporalConfluenceForecast() throws Exception {
        String createProfileJson = """
                {
                  "name": "Temporal Reference Native",
                  "dateOfBirth": "1990-05-15",
                  "timeOfBirth": "14:30:00",
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

        mockMvc.perform(get("/api/temporal/" + profileId)
                        .param("anchorTime", "2026-09-30T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.birthProfileId").value(profileId))
                .andExpect(jsonPath("$.data.windowCount").value(6))
                .andExpect(jsonPath("$.data.domainSummaries.length()").value(5))
                .andExpect(jsonPath("$.data.timelineWindows.length()").value(6))
                .andExpect(jsonPath("$.data.bestOverallWindowLabel").isNotEmpty());
    }
}
