package com.astroai.birth;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BirthProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullCrudLifecycleWithLocationAndTimezoneResolution() throws Exception {
        // 1. Create Birth Profile for New Delhi (1990-05-15 14:30 IST -> 09:00 UTC)
        String createPayload = """
                {
                  "name": "Aarav Sharma",
                  "dateOfBirth": "1990-05-15",
                  "timeOfBirth": "14:30:00",
                  "placeOfBirth": "New Delhi",
                  "gender": "MALE"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Aarav Sharma"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Kolkata"))
                .andExpect(jsonPath("$.data.utcOffsetHours").value(5.5))
                .andExpect(jsonPath("$.data.utcBirthTime").value("1990-05-15T09:00:00Z"))
                .andExpect(jsonPath("$.data.birthTimeAccurate").value(true))
                .andReturn();

        JsonNode root = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String profileId = root.path("data").path("id").asText();
        assertThat(profileId).isNotBlank();

        // 2. Get Profile by ID
        mockMvc.perform(get("/api/birth-profiles/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(profileId))
                .andExpect(jsonPath("$.data.placeOfBirth").value("New Delhi, Delhi"));

        // 3. Update Profile to Mumbai during Historical Indian War Time (1944-08-20 08:11 -> UTC+06:30)
        String updatePayload = """
                {
                  "name": "Aarav Sharma Updated",
                  "dateOfBirth": "1944-08-20",
                  "timeOfBirth": "08:11:00",
                  "placeOfBirth": "Mumbai",
                  "gender": "MALE"
                }
                """;

        mockMvc.perform(put("/api/birth-profiles/" + profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Aarav Sharma Updated"))
                .andExpect(jsonPath("$.data.utcOffsetHours").value(6.5))
                .andExpect(jsonPath("$.data.utcBirthTime").value("1944-08-20T01:41:00Z"));

        // 4. Delete Profile
        mockMvc.perform(delete("/api/birth-profiles/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 5. Verify 404 after deletion
        mockMvc.perform(get("/api/birth-profiles/" + profileId))
                .andExpect(status().isNotFound());
    }

    @Test
    void handlesMissingBirthTimeWithExplicitWarningAndRejectsInvalidLocation() throws Exception {
        String missingTimePayload = """
                {
                  "name": "Meera Iyer",
                  "dateOfBirth": "1998-11-03",
                  "placeOfBirth": "Chennai",
                  "gender": "FEMALE"
                }
                """;

        mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingTimePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.birthTimeAccurate").value(false))
                .andExpect(jsonPath("$.data.calculationWarnings[0]").exists());

        String invalidPlacePayload = """
                {
                  "name": "Invalid Place Test",
                  "dateOfBirth": "1998-11-03",
                  "timeOfBirth": "10:15:00",
                  "placeOfBirth": "Atlantis Unknown City XYZ",
                  "gender": "OTHER"
                }
                """;

        mockMvc.perform(post("/api/birth-profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPlacePayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
