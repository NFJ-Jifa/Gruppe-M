package com.gruppeM.energy_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * DTO used to carry percentage-based energy data.
 * This includes energy loss and grid usage share for a given hour.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown fields during deserialization
public class PercentageData {

    /**
     * The hour this percentage data belongs to.
     * Acts as a time-based identifier.
     */
    private Instant hourKey;

    /**
     * The amount of community-produced energy that was not used (kWh).
     */
    private double communityDepleted;

    /**
     * The portion (in percent or fraction) of energy that came from the public grid.
     */
    private double gridPortion;

    /**
     * Default constructor for Jackson or frameworks.
     */
    public PercentageData() {}

    /**
     * Full constructor for manually creating instances.
     */
    public PercentageData(Instant hourKey, double communityDepleted, double gridPortion) {
        this.hourKey = hourKey;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    // === Getters and Setters ===

    public Instant getHourKey() {
        return hourKey;
    }

    public void setHourKey(Instant hourKey) {
        this.hourKey = hourKey;
    }

    public double getCommunityDepleted() {
        return communityDepleted;
    }

    public void setCommunityDepleted(double communityDepleted) {
        this.communityDepleted = communityDepleted;
    }

    public double getGridPortion() {
        return gridPortion;
    }

    public void setGridPortion(double gridPortion) {
        this.gridPortion = gridPortion;
    }
}
