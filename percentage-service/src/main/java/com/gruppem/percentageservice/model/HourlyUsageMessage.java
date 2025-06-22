package com.gruppem.percentageservice.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyUsageMessage {

    // Принимаем и hourKey, и hour
    @JsonAlias({ "hourKey", "hour" })
    private Instant hourKey;

    private double communityProduced;
    private double communityUsed;
    private double gridUsed;

    public HourlyUsageMessage() {}

    // геттеры/сеттеры

    public Instant getHourKey() {
        return hourKey;
    }
    public void setHourKey(Instant hourKey) {
        this.hourKey = hourKey;
    }

    public double getCommunityProduced() {
        return communityProduced;
    }
    public void setCommunityProduced(double communityProduced) {
        this.communityProduced = communityProduced;
    }

    public double getCommunityUsed() {
        return communityUsed;
    }
    public void setCommunityUsed(double communityUsed) {
        this.communityUsed = communityUsed;
    }

    public double getGridUsed() {
        return gridUsed;
    }
    public void setGridUsed(double gridUsed) {
        this.gridUsed = gridUsed;
    }
}
