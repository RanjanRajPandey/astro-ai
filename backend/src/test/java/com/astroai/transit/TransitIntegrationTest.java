package com.astroai.transit;

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
class TransitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransitRepository transitRepository;

    @Test
    void calculatesAndPersistsGocharTransitsSadeSatiAndDoubleTransit() throws Exception {
        String createProfileJson = """
                {
                  "name": "Transit Reference Native",
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

        mockMvc.perform(get("/api/transits/" + profileId)
                        .param("transitTime", "2026-09-30T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.birthProfileId").value(profileId))
                .andExpect(jsonPath("$.data.transitUtcDatetimeIso").value("2026-09-30T12:00:00Z"))
                .andExpect(jsonPath("$.data.natalAscendantSign").value("Virgo"))
                .andExpect(jsonPath("$.data.natalMoonSign").value("Capricorn"))
                .andExpect(jsonPath("$.data.planets.length()").value(9))
                .andExpect(jsonPath("$.data.doubleTransitHouses.length()").value(12))
                .andExpect(jsonPath("$.data.sadeSati.phase").isNotEmpty());

        assertThat(transitRepository.findByBirthProfileId(java.util.UUID.fromString(profileId))).hasSize(1);
    }
}
