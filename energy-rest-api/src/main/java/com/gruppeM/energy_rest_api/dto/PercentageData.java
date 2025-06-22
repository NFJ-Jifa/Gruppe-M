package com.gruppeM.energy_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PercentageData {

    private Instant hourKey;
    private double percentage;

    public PercentageData() {}

    public PercentageData(Instant hourKey, double percentage) {
        this.hourKey    = hourKey;
        this.percentage = percentage;
    }

    public Instant getHourKey() {
        return hourKey;
    }

    public void setHourKey(Instant hourKey) {
        this.hourKey = hourKey;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
