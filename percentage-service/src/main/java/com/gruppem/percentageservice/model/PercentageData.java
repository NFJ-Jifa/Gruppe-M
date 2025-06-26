package com.gruppem.percentageservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Data model representing percentage data of energy usage.
 */
@Getter
@Setter
@NoArgsConstructor
public class PercentageData {

    private Instant hourKey;
    private double communityDepleted;
    private double gridPortion;

    /**
     * All-args constructor.
     */
    public PercentageData(Instant hourKey, double communityDepleted, double gridPortion) {
        this.hourKey = hourKey;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }
}
