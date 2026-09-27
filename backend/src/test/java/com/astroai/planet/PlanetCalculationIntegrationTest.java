package com.astroai.planet;

import com.fasterxml.jackson.databind.JsonNode;
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
class PlanetCalculationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlanetPositionRepository planetPositionRepository;

    @Test
    void calculatesAndPersistsDeterministicPlanetaryPositionsViaSwissEphemeris() throws Exception {
        String createProfileJson = """
                {
                  "name": "Golden Reference Native",
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

        MvcResult planetsResult = mockMvc.perform(get("/api/planets/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.birthProfileId").value(profileId))
                .andExpect(jsonPath("$.data.ayanamshaType").value("LAHIRI"))
                .andExpect(jsonPath("$.data.ascendantSign").value("Virgo"))
                .andExpect(jsonPath("$.data.planets.length()").value(9))
                .andReturn();

        JsonNode dataNode = objectMapper.readTree(planetsResult.getResponse().getContentAsString()).path("data");
        String chartId = dataNode.path("chartId").asText();
        assertThat(chartId).isNotBlank();

        // Verify all 9 planets were persisted in planet_positions table
        assertThat(planetPositionRepository.findAll()).hasSizeGreaterThanOrEqualTo(9);
    }
}
