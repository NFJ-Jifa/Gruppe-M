package com.gruppem.usage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Usage Service application.
 *
 * This class bootstraps the Spring Boot application, enabling component scanning,
 * auto-configuration, and starting the embedded web server or listener infrastructure
 * for the energy usage processing system.
 */
@SpringBootApplication
public class UsageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UsageServiceApplication.class, args);
    }
}
