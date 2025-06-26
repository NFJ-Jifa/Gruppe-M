package com.gruppem.energygui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * JavaFX data model class used for displaying energy data in the TableView.
 * Wraps hour, communityDepleted, and gridPortion values as observable properties.
 */
public class EnergyDataFX {

    /** The hour of the energy measurement (formatted for display) */
    private final SimpleStringProperty hour;

    /** Percentage of community-produced energy that was consumed */
    private final SimpleDoubleProperty communityDepleted;

    /** Percentage of total energy that came from the public grid */
    private final SimpleDoubleProperty gridPortion;

    /**
     * Constructor to initialize all properties
     * @param hour formatted hour string
     * @param communityDepleted percentage of used community energy
     * @param gridPortion percentage of energy from the grid
     */
    public EnergyDataFX(String hour, double communityDepleted, double gridPortion) {
        this.hour = new SimpleStringProperty(hour);
        this.communityDepleted = new SimpleDoubleProperty(communityDepleted);
        this.gridPortion = new SimpleDoubleProperty(gridPortion);
    }

    // === Getters for JavaFX bindings ===

    /** Returns the formatted hour string */
    public String getHour() {
        return hour.get();
    }

    /** Returns the percentage of community energy used */
    public double getCommunityDepleted() {
        return communityDepleted.get();
    }

    /** Returns the percentage of grid energy used */
    public double getGridPortion() {
        return gridPortion.get();
    }
}
