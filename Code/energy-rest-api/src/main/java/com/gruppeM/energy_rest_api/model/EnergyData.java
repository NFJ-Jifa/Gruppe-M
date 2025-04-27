package com.gruppeM.energy_rest_api.model;

import java.time.LocalDateTime;

public class EnergyData {
    private LocalDateTime hour;
    private double communityDepleted;
    private double gridPortion;

    public EnergyData(LocalDateTime hour, double communityDepleted, double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public LocalDateTime getHour() {
        return hour;
    }

    public double getCommunityDepleted() {
        return communityDepleted;
    }

    public double getGridPortion() {
        return gridPortion;
    }
}
