package com.gruppem.energygui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class EnergyDataFX {
    private final SimpleStringProperty hour;
    private final SimpleDoubleProperty communityDepleted;
    private final SimpleDoubleProperty gridPortion;

    public EnergyDataFX(String hour, double communityDepleted, double gridPortion) {
        this.hour = new SimpleStringProperty(hour);
        this.communityDepleted = new SimpleDoubleProperty(communityDepleted);
        this.gridPortion = new SimpleDoubleProperty(gridPortion);
    }

    public String getHour() {
        return hour.get();
    }

    public double getCommunityDepleted() {
        return communityDepleted.get();
    }

    public double getGridPortion() {
        return gridPortion.get();
    }
}
