package com.gruppem.percentageservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor
public class PercentageData {
    private Instant hourKey;
    private double percentage;

    public PercentageData(Instant hourKey, double percentage) {
        this.hourKey    = hourKey;
        this.percentage = percentage;
    }
}
