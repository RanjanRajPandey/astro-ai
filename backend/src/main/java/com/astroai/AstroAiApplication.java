package com.astroai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AstroAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstroAiApplication.class, args);
    }
}
