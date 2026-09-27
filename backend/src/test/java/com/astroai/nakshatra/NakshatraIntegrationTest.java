package com.astroai.nakshatra;

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
class NakshatraIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NakshatraPlacementRepository nakshatraPlacementRepository;

    @Test
    void calculatesAndPersistsNakshatraPlacementsPadaNavamshaAndTaraBala() throws Exception {
        String createProfileJson = """
                {
                  "name": "Nakshatra Reference Native",
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

        mockMvc.perform(get("/api/nakshatra/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.janmaNakshatra").isNotEmpty())
                .andExpect(jsonPath("$.data.janmaNakshatraLord").isNotEmpty())
                .andExpect(jsonPath("$.data.janmaRashi").isNotEmpty())
                .andExpect(jsonPath("$.data.placements.length()").value(10))
                .andExpect(jsonPath("$.data.placements[0].bodyName").value("Ascendant"))
                .andExpect(jsonPath("$.data.placements[0].padaNavamshaSign").isNotEmpty())
                .andExpect(jsonPath("$.data.placements[0].taraNameFromMoon").isNotEmpty());

        assertThat(nakshatraPlacementRepository.findAll()).hasSizeGreaterThanOrEqualTo(10);
    }
}
