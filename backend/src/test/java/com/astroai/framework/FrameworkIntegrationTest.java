package com.astroai.framework;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FrameworkIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void classifiesQuestionsAndLoadsNineVedicAnalysisFrameworks() throws Exception {
        mockMvc.perform(get("/api/frameworks")
                        .param("question", "When will I get married and how is my spouse compatibility?"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.primaryCategory").value("MARRIAGE_AND_RELATIONSHIPS"))
                .andExpect(jsonPath("$.data.allFrameworks.length()").value(9))
                .andExpect(jsonPath("$.data.activeFramework.checklistRules").isNotEmpty());

        mockMvc.perform(post("/api/frameworks/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionText\":\"When will I get a job promotion and leadership authority?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.primaryCategory").value("CAREER_AND_PROFESSION"));
    }
}
