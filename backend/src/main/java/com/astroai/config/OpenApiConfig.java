package com.astroai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI astroAiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Astro-AI Vedic Astrology & Explainable AI API")
                        .version("0.1.0")
                        .description("Deterministic Vedic Astrology / Kundli Analysis & Traceable AI Reasoning API"));
    }
}
