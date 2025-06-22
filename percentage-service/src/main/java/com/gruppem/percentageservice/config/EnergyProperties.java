package com.gruppem.percentageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "energy")
public class EnergyProperties {
    private String updateQueue;
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
