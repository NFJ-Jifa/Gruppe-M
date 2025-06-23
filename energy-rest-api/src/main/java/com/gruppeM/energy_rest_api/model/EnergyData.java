package com.gruppeM.energy_rest_api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnergyData {

    /**
     * Поле hour теперь хранит Instant напрямую.
     * Форматируем его в ISO-строку с суффиксом "Z".
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant hour;
    private double communityDepleted;
    private double gridPortion;

    // Для Jackson
    public EnergyData() {}

    @JsonCreator
    public EnergyData(
            @JsonProperty("hour") Instant hour,
            @JsonProperty("communityDepleted") double communityDepleted,
            @JsonProperty("gridPortion") double gridPortion
    ) {
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
