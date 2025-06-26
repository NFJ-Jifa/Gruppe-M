package com.gruppem.usage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Entity class representing hourly energy usage and production data.
 */
@Entity
@Table(name = "hourly_usage")
public class HourlyUsage {

    @Id
    @Column(name = "hour_ts", nullable = false, updatable = false)
    private Instant hourKey;  // Unique timestamp representing the hour (truncated to the hour)

    @Column(name = "community_produced", nullable = false)
    private double communityProduced = 0;  // Energy produced by the community in kWh

    @Column(name = "community_used", nullable = false)
    private double communityUsed = 0;  // Energy used by the community in kWh

    @Column(name = "grid_used", nullable = false)
    private double gridUsed = 0;  // Energy drawn from the external grid in kWh

    /** Default constructor required by JPA */
    public HourlyUsage() {}

    /**
     * Constructor that initializes the hour key by truncating the given timestamp to the hour.
     * @param timestamp The time to truncate and use as hour key.
     */
    public HourlyUsage(Instant timestamp) {
        this.hourKey = timestamp.truncatedTo(ChronoUnit.HOURS);
    }

    // ======== Getters and Setters ========

    public Instant getHourKey() { return hourKey; }

    public void setHourKey(Instant hourKey) { this.hourKey = hourKey; }

    public double getCommunityProduced() { return communityProduced; }

    public void setCommunityProduced(double communityProduced) { this.communityProduced = communityProduced; }

    public double getCommunityUsed() { return communityUsed; }

    public void setCommunityUsed(double communityUsed) { this.communityUsed = communityUsed; }

    public double getGridUsed() { return gridUsed; }

    public void setGridUsed(double gridUsed) { this.gridUsed = gridUsed; }
}
