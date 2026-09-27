package com.astroai.house;

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
class HouseCalculationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HouseRepository houseRepository;

    @Test
    void calculatesAndPersistsAscendantSpecialLagnasAnd12Houses() throws Exception {
        String createProfileJson = """
                {
                  "name": "House Engine Reference Native",
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

        mockMvc.perform(get("/api/houses/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.houseSystem").value("WHOLE_SIGN_WITH_SRIPATI"))
                .andExpect(jsonPath("$.data.ascendant.sign").value("Virgo"))
                .andExpect(jsonPath("$.data.ascendant.lagnaLord").value("Mercury"))
                .andExpect(jsonPath("$.data.ascendant.arudhaLagnaSign").isNotEmpty())
                .andExpect(jsonPath("$.data.ascendant.upapadaLagnaSign").isNotEmpty())
                .andExpect(jsonPath("$.data.houses.length()").value(12))
                .andExpect(jsonPath("$.data.houses[0].houseNumber").value(1))
                .andExpect(jsonPath("$.data.houses[0].sign").value("Virgo"))
                .andExpect(jsonPath("$.data.houses[8].houseNumber").value(9))
                .andExpect(jsonPath("$.data.houses[8].sign").value("Taurus"));

        assertThat(houseRepository.findAll()).hasSizeGreaterThanOrEqualTo(12);
    }
}
