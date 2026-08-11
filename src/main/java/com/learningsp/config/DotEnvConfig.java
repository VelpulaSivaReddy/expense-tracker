package com.learningsp.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DotEnvConfig {
    
    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            // Load environment variables from .env file
            dotenv.entries().forEach(entry -> {
                if (System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
            
            log.info("DotEnv configuration loaded successfully");
        } catch (Exception e) {
            log.warn("Failed to load .env file: {}", e.getMessage());
        }
    }
}
