package com.gruppem.percentageservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
public class PercentageData {
    private Instant hourKey;
    private double gridPercentage;

    public PercentageData(Instant hourKey, double gridPercentage) {
        this.hourKey = hourKey;
        this.gridPercentage = gridPercentage;
    }
}
