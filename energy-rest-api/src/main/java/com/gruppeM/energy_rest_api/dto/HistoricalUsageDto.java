package com.gruppeM.energy_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class HistoricalUsageDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant hour;
    private double communityProduced;
    private double communityUsed;
    private double gridUsed;
    private double communityDepleted;
    private double gridPortion;

    public HistoricalUsageDto() {}

    public HistoricalUsageDto(Instant hour,
                              double communityProduced,
                              double communityUsed,
                              double gridUsed,
                              double communityDepleted,
                              double gridPortion) {
        this.hour = hour;
        this.communityProduced = communityProduced;
        this.communityUsed = communityUsed;
        this.gridUsed = gridUsed;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    // геттеры/сеттеры для всех полей
    public Instant getHour() { return hour; }
    public void setHour(Instant hour) { this.hour = hour; }
    public double getCommunityProduced() { return communityProduced; }
    public void setCommunityProduced(double communityProduced) { this.communityProduced = communityProduced; }
    public double getCommunityUsed() { return communityUsed; }
    public void setCommunityUsed(double communityUsed) { this.communityUsed = communityUsed; }
    public double getGridUsed() { return gridUsed; }
    public void setGridUsed(double gridUsed) { this.gridUsed = gridUsed; }
    public double getCommunityDepleted() { return communityDepleted; }
    public void setCommunityDepleted(double communityDepleted) { this.communityDepleted = communityDepleted; }
    public double getGridPortion() { return gridPortion; }
    public void setGridPortion(double gridPortion) { this.gridPortion = gridPortion; }
}
