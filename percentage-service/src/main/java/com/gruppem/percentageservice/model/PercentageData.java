package com.gruppem.percentageservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
public class PercentageData {

    private Instant hourKey;
    private double communityDepleted;
    private double gridPortion;

    public PercentageData(Instant hourKey, double communityDepleted, double gridPortion) {
        this.hourKey           = hourKey;
        this.communityDepleted = communityDepleted;
        this.gridPortion       = gridPortion;
    }
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