package com.gruppem.percentageservice.model;

import java.time.Instant;

/**
 * Model class representing aggregated energy data for a specific hour.
 *
 * Contains information about the amount of energy depleted from the community
 * and the portion taken from the grid.
 */
public class EnergyData {

    // The hour timestamp (truncated to hour precision)
    private Instant hour;

    // Amount of energy depleted from the community in kWh
    private double communityDepleted;

    // Amount of energy taken from the grid in kWh
    private double gridPortion;

    /**
     * Default constructor for serialization/deserialization.
     */
    public EnergyData() {}

    /**
     * Full constructor to initialize all fields.
     *
     * @param hour             The timestamp representing the hour
     * @param communityDepleted The amount of energy depleted from the community
     * @param gridPortion      The amount of energy taken from the grid
     */
    public EnergyData(Instant hour, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public Instant getHour() {
        return hour;
    }

    public void setHour(Instant hour) {
        this.hour = hour;
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
