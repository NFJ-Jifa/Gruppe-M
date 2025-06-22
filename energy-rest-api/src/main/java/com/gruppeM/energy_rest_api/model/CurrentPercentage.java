package com.gruppeM.energy_rest_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.Instant;


@Entity
@Table(name = "current_percentage")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentPercentage  {

    @Id
    @Column(name = "hour_ts", nullable = false)
    private Instant hourKey;

    @Column(name = "community_depleted", nullable = false)
    private double communityDepleted;

    @Column(name = "grid_portion", nullable = false)
    private double gridPortion;

    protected CurrentPercentage() {}

    public CurrentPercentage(Instant hourKey,
                             double communityDepleted,
                             double gridPortion) {
        this.hourKey          = hourKey;
        this.communityDepleted = communityDepleted;
        this.gridPortion      = gridPortion;
    }

    public Instant getHourKey() {
        return hourKey;
    }

    public double getCommunityDepleted() {
        return communityDepleted;
    }

    public double getGridPortion() {
        return gridPortion;
    }
}
