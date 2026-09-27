package com.astroai.dasha;

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
class DashaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DashaPeriodRepository dashaPeriodRepository;

    @Test
    void calculatesAndPersists5LevelVimshottariDashaTimelineAndActiveStack() throws Exception {
        String createProfileJson = """
                {
                  "name": "Vimshottari Dasha Reference Native",
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

        mockMvc.perform(get("/api/dashas/" + profileId)
                        .param("targetTime", "2026-09-28T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.yearLengthDays").value(365.2425))
                .andExpect(jsonPath("$.data.birthDashaLord").isNotEmpty())
                .andExpect(jsonPath("$.data.birthBalanceFormatted").isNotEmpty())
                .andExpect(jsonPath("$.data.activeStack.length()").value(5))
                .andExpect(jsonPath("$.data.activeStack[0].levelName").value("MAHADASHA"))
                .andExpect(jsonPath("$.data.activeStack[1].levelName").value("ANTARDASHA"))
                .andExpect(jsonPath("$.data.activeStack[2].levelName").value("PRATYANTARDASHA"))
                .andExpect(jsonPath("$.data.activeStack[3].levelName").value("SOOKSHMA_DASHA"))
                .andExpect(jsonPath("$.data.activeStack[4].levelName").value("PRANA_DASHA"))
                .andExpect(jsonPath("$.data.mahadashas.length()").value(9))
                .andExpect(jsonPath("$.data.mahadashas[1].subPeriods.length()").value(9))
                .andExpect(jsonPath("$.data.mahadashas[1].subPeriods[0].subPeriods.length()").value(9));

        assertThat(dashaPeriodRepository.findAll()).hasSizeGreaterThanOrEqualTo(80);
    }
}
