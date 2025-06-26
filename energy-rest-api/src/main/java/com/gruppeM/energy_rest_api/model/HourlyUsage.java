package com.gruppeM.energy_rest_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * JPA Entity for storing hourly aggregated energy usage data.
 * Each record represents energy stats for one specific hour.
 */
@Entity
@Table(name = "hourly_usage")
public class HourlyUsage {

    /**
     * Primary key representing the start of the hour (truncated timestamp).
     * Stored as column 'hour_ts'.
     */
    @Id
    @Column(name = "hour_ts", nullable = false, updatable = false)
    private Instant hourKey;

    /**
     * Total energy (in kWh) produced by the community during this hour.
     */
    @Column(name = "community_produced", nullable = false)
    private double communityProduced = 0;

    /**
     * Total energy (in kWh) consumed from the community pool in this hour.
     */
    @Column(name = "community_used", nullable = false)
    private double communityUsed = 0;

    /**
     * Total energy (in kWh) drawn from the public grid due to community depletion.
     */
    @Column(name = "grid_used", nullable = false)
    private double gridUsed = 0;

    /**
     * Default constructor for JPA (required).
     */
    public HourlyUsage() {}

    /**
     * Constructor that automatically truncates the timestamp to the hour.
     * Ensures consistent grouping of data per hour.
     *
     * @param timestamp Any timestamp within the hour (will be truncated)
     */
    public HourlyUsage(Instant timestamp) {
        this.hourKey = timestamp.truncatedTo(ChronoUnit.HOURS);
    }

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
