package com.gruppeM.energy_rest_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the Spring Boot application.
 * This class launches the REST API server.
 *
 * The @SpringBootApplication annotation combines:
 * - @Configuration: allows bean definition
 * - @EnableAutoConfiguration: enables Spring Boot’s auto-configuration
 * - @ComponentScan: scans the package for @Component/@Service/@Repository/@Controller
 */
@SpringBootApplication
public class EnergyRestApiApplication {

	/**
	 * Starts the Spring Boot application.
	 * Initializes the embedded Tomcat server and all Spring contexts.
	 *
	 * @param args optional command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(EnergyRestApiApplication.class, args);
	}
}
