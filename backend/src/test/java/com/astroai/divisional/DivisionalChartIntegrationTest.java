package com.astroai.divisional;

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
class DivisionalChartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DivisionalChartRepository divisionalChartRepository;

    @Test
    void calculatesAndPersistsAll16ShodashavargaChartsAndSingleVargaLookup() throws Exception {
        String createProfileJson = """
                {
                  "name": "Shodashavarga Reference Native",
                  "dateOfBirth": "1990-05-15",
                  "timeOfBirth": "14:30:00",
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

        mockMvc.perform(get("/api/charts/" + profileId + "/divisional"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.charts.length()").value(16))
                .andExpect(jsonPath("$.data.charts[0].vargaCode").value("D1"))
                .andExpect(jsonPath("$.data.charts[5].vargaCode").value("D9"))
                .andExpect(jsonPath("$.data.charts[6].vargaCode").value("D10"))
                .andExpect(jsonPath("$.data.charts[15].vargaCode").value("D60"))
                .andExpect(jsonPath("$.data.charts[15].planets[0].shashtiamshaName").isNotEmpty());

        mockMvc.perform(get("/api/charts/" + profileId + "/divisional/D9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.vargaCode").value("D9"))
                .andExpect(jsonPath("$.data.sanskritName").value("Navamsha"))
                .andExpect(jsonPath("$.data.planets.length()").value(9));

        assertThat(divisionalChartRepository.findAll()).hasSizeGreaterThanOrEqualTo(16);
    }
}
