
package com.gruppem.percentageservice.model;

import java.time.Instant;

public class EnergyData {
    private Instant hour;
    private double communityDepleted;
    private double gridPortion;

    public EnergyData() {}

    public EnergyData(Instant hour, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public Instant getHour() { return hour; }
    public void setHour(Instant hour) { this.hour = hour; }

    public double getCommunityDepleted() { return communityDepleted; }
    public void setCommunityDepleted(double communityDepleted) { this.communityDepleted = communityDepleted; }

    public double getGridPortion() { return gridPortion; }
    public void setGridPortion(double gridPortion) { this.gridPortion = gridPortion; }
}
