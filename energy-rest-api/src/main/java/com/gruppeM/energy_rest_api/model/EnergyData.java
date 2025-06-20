package com.gruppeM.energy_rest_api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class EnergyData {
    private LocalDateTime hour;
    private double communityDepleted;
    private double gridPortion;

    // нужен для Jackson
    public EnergyData() {}

    @JsonCreator
    public EnergyData(@JsonProperty("hour") LocalDateTime hour,
                      @JsonProperty("communityDepleted") double communityDepleted,
                      @JsonProperty("gridPortion") double gridPortion) {
        this.hour = hour;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    public LocalDateTime getHour() {
        return hour;
    }
    public void setHour(LocalDateTime hour) {
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
