package com.gruppeM.energy_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * DTO representing summarized hourly energy usage data.
 * Typically received from another service via RabbitMQ.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // ignore unexpected fields during deserialization
public class UsageMessageDto {

    /**
     * Timestamp representing the hour this data belongs to.
     * The JSON input may use either "hourKey" or "hour" as the field name.
     */
    @JsonAlias({"hourKey", "hour"})
    private Instant hourKey;

    /**
     * Total amount of energy (in kWh) produced by the community during this hour.
     */
    private double communityProduced;

    /**
     * Total amount of energy (in kWh) consumed by the community during this hour.
     */
    private double communityUsed;

    /**
     * Total amount of energy (in kWh) drawn from the public grid during this hour.
     */
    private double gridUsed;

    /**
     * Default constructor required for deserialization.
     */
    public UsageMessageDto() {}

    // === Getters and Setters ===

    public Instant getHourKey() {
        return hourKey;
    }
    public void setHourKey(Instant hourKey) {
        this.hourKey = hourKey;
    }

    public double getCommunityProduced() {
        return communityProduced;
    }
    public void setCommunityProduced(double communityProduced) {
        this.communityProduced = communityProduced;
    }

    public double getCommunityUsed() {
        return communityUsed;
    }
    public void setCommunityUsed(double communityUsed) {
        this.communityUsed = communityUsed;
    }

    public double getGridUsed() {
        return gridUsed;
    }
    public void setGridUsed(double gridUsed) {
        this.gridUsed = gridUsed;
    }
}
