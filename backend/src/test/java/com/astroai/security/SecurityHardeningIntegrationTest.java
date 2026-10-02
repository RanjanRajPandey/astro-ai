package com.astroai.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
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
class SecurityHardeningIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @AfterEach
    void tearDown() {
        rateLimitingFilter.resetLimits();
    }

    @Test
    @DisplayName("Verify Security Response Headers: FrameOptions, ContentTypeOptions, and CSP")
    void testSecurityHeadersPresent() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    @DisplayName("Verify Rate Limiter Triggers 429 Too Many Requests upon Burst Overflow")
    void testRateLimiterTriggers429() throws Exception {
        // Set low threshold for testing: 3 requests per 10 seconds
        rateLimitingFilter.setLimitForTesting(3, 10_000L);

        String loginPayload = """
                {
                  "email": "ratelimit_test@astroai.com",
                  "password": "WrongPassword123!"
                }
                """;

        // First 3 requests get through to the controller (returning 401 for bad credentials)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginPayload))
                    .andExpect(status().isUnauthorized());
        }

        // 4th request MUST be blocked by RateLimitingFilter with HTTP 429 Too Many Requests
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Rate limit exceeded. Please wait 60 seconds before making further requests."));
    }

    @Test
    @DisplayName("Verify GDPR Right to Erasure: Account Deletion Cascades and Purges User")
    void testAccountDeletionCascades() throws Exception {
        String email = "privacy_delete_" + System.currentTimeMillis() + "@astroai.com";
        String registerJson = """
                {
                  "email": "%s",
                  "password": "SecurePassword123!",
                  "full_name": "Private Native",
                  "role": "USER"
                }
                """.formatted(email);

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString())
                .path("data").path("access_token").asText();

        // Check user profile exists
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Perform Right to Erasure (DELETE /api/auth/me)
        mockMvc.perform(delete("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Subsequent access with same token or login fails
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Verify GDPR Anonymization: Masks Personal Identity with Pseudonym")
    void testAccountAnonymization() throws Exception {
        String email = "anonymize_" + System.currentTimeMillis() + "@astroai.com";
        String registerJson = """
                {
                  "email": "%s",
                  "password": "SecurePassword123!",
                  "full_name": "Siddhartha Gautama",
                  "role": "USER"
                }
                """.formatted(email);

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString())
                .path("data").path("access_token").asText();

        // Anonymize Account
        mockMvc.perform(post("/api/auth/me/anonymize")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.full_name").value(org.hamcrest.Matchers.startsWith("Native-")))
                .andExpect(jsonPath("$.data.email").value(org.hamcrest.Matchers.endsWith("@privacy.local")));
    }
}
