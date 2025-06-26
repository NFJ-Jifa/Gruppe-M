package com.gruppeM.energy_rest_api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Model class representing the most recent energy statistics.
 * This data is returned by the /energy/current endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnergyData {

    /**
     * The hour this data applies to, formatted as an ISO-8601 string with "Z" (UTC).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant hour;

    /**
     * Amount of energy (kWh) produced by the community but not used.
     */
    private double communityDepleted;

    /**
     * Fraction (or percentage) of total energy consumption coming from the public grid.
     */
    private double gridPortion;

    /**
     * Default constructor required for Jackson and frameworks.
     */
    public EnergyData() {}

    /**
     * Constructor annotated with @JsonCreator to control how the JSON is deserialized.
     *
     * @param hour               Hour timestamp (parsed from "hour" field in JSON)
     * @param communityDepleted  Unused community energy
     * @param gridPortion        Portion of energy from the grid
     */
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

    // === Getters and Setters ===

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
