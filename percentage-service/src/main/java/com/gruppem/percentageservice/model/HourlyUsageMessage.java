package com.gruppem.percentageservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * DTO for hourly usage messages.
 *
 * Configured to ignore unknown JSON properties to allow flexible input.
 *
 * Supports deserialization of timestamp fields named either "hourKey" or "hour"
 * via @JsonAlias.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyUsageMessage {

    // Accepts both "hourKey" and "hour" as JSON property names for this field
    @JsonAlias({ "hourKey", "hour" })
    private Instant hourKey;

    private double communityProduced;
    private double communityUsed;
    private double gridUsed;

    /**
     * Default constructor required for JSON deserialization.
     */
    public HourlyUsageMessage() {}

    // ===== Getters and Setters =====

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
