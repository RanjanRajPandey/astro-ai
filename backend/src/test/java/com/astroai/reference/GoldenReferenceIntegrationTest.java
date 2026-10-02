package com.astroai.reference;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoldenReferenceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Indian Independence Chart (Aug 15, 1947, 00:00 IST New Delhi) Golden Reference Validation")
    void testIndianIndependenceChartGoldenReference() throws Exception {
        String profileJson = """
                {
                  "name": "Indian Independence Golden Reference",
                  "dateOfBirth": "1947-08-15",
                  "timeOfBirth": "00:00:00",
                  "placeOfBirth": "New Delhi",
                  "gender": "OTHER"
                }
                """;

        // 1. Create Profile
        MvcResult profileResult = mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileJson))
                .andExpect(status().isCreated())
                .andReturn();

        String profileId = objectMapper.readTree(profileResult.getResponse().getContentAsString())
                .path("data").path("id").asText();

        // 2. D1 Kundli Chart: Taurus Lagna, Pushya Nakshatra
        mockMvc.perform(get("/api/charts/" + profileId + "/d1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ascendant.sign").value("Taurus"))
                .andExpect(jsonPath("$.data.ascendant.sanskritSign").value("Vrishabha"))
                .andExpect(jsonPath("$.data.moonNakshatra").value("Pushya"))
                .andExpect(jsonPath("$.data.planets.length()").value(9))
                .andExpect(jsonPath("$.data.houses.length()").value(12));

        // 3. Vimshottari Dasha: Moon in Pushya -> Birth Mahadasha MUST be Saturn
        mockMvc.perform(get("/api/dashas/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.birthDashaLord").value("Saturn"))
                .andExpect(jsonPath("$.data.mahadashas.length()").value(9));

        // 4. Classical Yogas: Detect multiple yogas from 5-planet cluster and planetary alignments
        mockMvc.perform(get("/api/yogas/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.activeYogas").isArray());
    }
}
