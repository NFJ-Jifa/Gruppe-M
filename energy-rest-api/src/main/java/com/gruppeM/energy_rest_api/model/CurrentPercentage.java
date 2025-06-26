package com.gruppeM.energy_rest_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA Entity representing the most recently received percentage data
 * for a specific hour. This includes energy depletion and grid usage ratio.
 */
@Entity
@Table(name = "current_percentage")
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore extra JSON fields
public class CurrentPercentage  {

    /**
     * The timestamp of the hour this data belongs to.
     * Serves as the primary key in the database table.
     */
    @Id
    @Column(name = "hour_ts", nullable = false)
    private Instant hourKey;

    /**
     * Amount of energy produced by the community but not used (in kWh).
     */
    @Column(name = "community_depleted", nullable = false)
    private double communityDepleted;

    /**
     * Fraction of total energy consumption drawn from the public grid.
     */
    @Column(name = "grid_portion", nullable = false)
    private double gridPortion;

    /**
     * Protected default constructor required by JPA.
     */
    protected CurrentPercentage() {}

    /**
     * Constructor for manual instantiation.
     *
     * @param hourKey           Hour timestamp
     * @param communityDepleted Unused energy in kWh
     * @param gridPortion       Portion of energy from the grid
     */
    public CurrentPercentage(Instant hourKey,
                             double communityDepleted,
                             double gridPortion) {
        this.hourKey = hourKey;
        this.communityDepleted = communityDepleted;
        this.gridPortion = gridPortion;
    }

    // === Getters ===

    public Instant getHourKey() {
        return hourKey;
    }

    public double getCommunityDepleted() {
        return communityDepleted;
    }

    public double getGridPortion() {
        return gridPortion;
    }
}
