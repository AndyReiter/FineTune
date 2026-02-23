package com.finetune.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FineTuneApplication {
    public static void main(String[] args) {
        // Load .env file before Spring Boot initialization
        // This makes environment variables from .env available to Spring
        try {
            io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            // Set environment variables from .env file
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file - " + e.getMessage());
        }
        
        SpringApplication.run(FineTuneApplication.class, args);
    }
}
