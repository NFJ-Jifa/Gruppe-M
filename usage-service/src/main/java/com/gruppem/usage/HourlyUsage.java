package com.gruppem.usage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Entity to store hourly aggregated usage data.
 */
@Entity
@Table(name = "hourly_usage")
public class HourlyUsage {

    /** The hour truncated to start of the hour, stored in column hour_ts */
    @Id
    @Column(name = "hour_ts", nullable = false, updatable = false)
    private Instant hourKey;

    /** Energy produced by community in this hour */
    @Column(name = "community_produced", nullable = false)
    private double communityProduced = 0;

    /** Energy consumed from community pool */
    @Column(name = "community_used", nullable = false)
    private double communityUsed = 0;

    /** Energy drawn from grid when community pool was depleted */
    @Column(name = "grid_used", nullable = false)
    private double gridUsed = 0;

    // Default constructor for JPA
    public HourlyUsage() {}

    // Create new record for given timestamp (truncated to hour)
    public HourlyUsage(Instant timestamp) {
        this.hourKey = timestamp.truncatedTo(ChronoUnit.HOURS);
    }

    // Getters and setters

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
