package com.gruppem.percentageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties class for energy-related queue names.
 * <p>
 * This class binds properties with the prefix "energy" from the application's
 * configuration file (e.g., application.yml or application.properties).
 * </p>
 * Example:
 * <pre>
 * energy:
 *   update-queue: energy.update
 *   final-queue: energy.final
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "energy")
public class EnergyProperties {

    // Name of the queue receiving hourly updates
    private String updateQueue;

    // Name of the queue receiving final/aggregated results
    private String finalQueue;

    public String getUpdateQueue() {
        return updateQueue;
    }

    public void setUpdateQueue(String updateQueue) {
        this.updateQueue = updateQueue;
    }

    public String getFinalQueue() {
        return finalQueue;
    }

    public void setFinalQueue(String finalQueue) {
        this.finalQueue = finalQueue;
    }
}
