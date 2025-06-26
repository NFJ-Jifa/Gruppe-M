package com.gruppeM.energy_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * DTO used to return historical energy data to the frontend.
 * Each instance represents energy statistics for a specific hour.
 */
public class HistoricalUsageDto {

    /**
     * The hour this data belongs to.
     * Formatted as an ISO-8601 string in JSON.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant hour;

    /**
     * Total energy (in kWh) produced by the community during this hour.
     */
    private double communityProduced;

    /**
     * Total energy (in kWh) consumed by the community during this hour.
     */
    private double communityUsed;

    /**
     * Energy (in kWh) drawn from the public grid.
     */
    private double gridUsed;

    /**
     * Community energy that was not used and thus lost.
     */
    private double communityDepleted;

    /**
     * Percentage of total energy usage that came from the grid.
     */
    private double gridPortion;

    /**
     * Default constructor for JSON deserialization.
     */
    public HistoricalUsageDto() {}

    /**
     * Full constructor for manual creation.
     */
    public HistoricalUsageDto(Instant hour,
                              double communityProduced,
                              double communityUsed,
                              double gridUsed,
                              double communityDepleted,
                              double gridPortion) {
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    // === Getters and Setters ===

    public Instant getHour() { return hour; }
    public void setHour(Instant hour) { this.hour = hour; }

    public double getCommunityProduced() { return communityProduced; }
    public void setCommunityProduced(double communityProduced) { this.communityProduced = communityProduced; }

    public double getCommunityUsed() { return communityUsed; }
    public void setCommunityUsed(double communityUsed) { this.communityUsed = communityUsed; }

    public double getGridUsed() { return gridUsed; }
    public void setGridUsed(double gridUsed) { this.gridUsed = gridUsed; }

    public double getCommunityDepleted() { return communityDepleted; }
    public void setCommunityDepleted(double communityDepleted) { this.communityDepleted = communityDepleted; }

    public double getGridPortion() { return gridPortion; }
    public void setGridPortion(double gridPortion) { this.gridPortion = gridPortion; }
}
