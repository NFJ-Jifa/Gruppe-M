package com.gruppem.percentageservice.dto;

import java.time.Instant;

/**
 * Data Transfer Object (DTO) representing an energy event,
 * either from a producer or a user.
 *
 * This message is typically transferred over RabbitMQ in JSON format.
 */
public class EnergyMessage {

    private String type;        // "PRODUCER" or "USER"
    private String association; // "COMMUNITY" or "GRID"
    private double kwh;         // Amount of energy in kilowatt-hours
    private Instant datetime;   // Timestamp when the energy event occurred

    /**
     * Default constructor required for deserialization (e.g., from JSON).
     */
    public EnergyMessage() {}

    // ======== Getters and Setters ========

    /**
     * Returns the type of message: "PRODUCER" or "USER".
     */
    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    /**
     * Returns the association type: "COMMUNITY" or "GRID".
     */
    public String getAssociation() { return association; }

    public void setAssociation(String association) { this.association = association; }

    /**
     * Returns the energy amount in kWh.
     */
    public double getKwh() { return kwh; }

    public void setKwh(double kwh) { this.kwh = kwh; }

    /**
     * Returns the timestamp of the energy event.
     */
    public Instant getDatetime() { return datetime; }

    public void setDatetime(Instant datetime) { this.datetime = datetime; }
}
