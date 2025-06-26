package com.gruppeM.energy_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

/**
 * DTO (Data Transfer Object) representing a raw energy message.
 * This message is sent by a producer and received via RabbitMQ.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // allows flexible deserialization
public class EnergyMessage {

    private String type;          // Type of energy message (e.g. "PRODUCTION" or "USAGE")
    private String association;   // Name or ID of the energy community
    private double kwh;           // Energy value in kilowatt-hours
    private Instant datetime;     // Timestamp of the event

    /**
     * Default constructor (required for Jackson deserialization).
     */
    public EnergyMessage() { }

    /**
     * Full constructor for manual object creation.
     *
     * @param type        type of energy event
     * @param association name of the energy community
     * @param kwh         amount of energy
     * @param datetime    timestamp of the event
     */
    public EnergyMessage(String type, String association, double kwh, Instant datetime) {
        this.type        = type;
        this.association = association;
        this.kwh         = kwh;
        this.datetime    = datetime;
    }

    // Getter and Setter for 'type'
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    // Getter and Setter for 'association'
    public String getAssociation() {
        return association;
    }
    public void setAssociation(String association) {
        this.association = association;
    }

    // Getter and Setter for 'kwh'
    public double getKwh() {
        return kwh;
    }
    public void setKwh(double kwh) {
        this.kwh = kwh;
    }

    // Getter and Setter for 'datetime'
    public Instant getDatetime() {
        return datetime;
    }
    public void setDatetime(Instant datetime) {
        this.datetime = datetime;
    }

    /**
     * Debug-friendly string representation of this message.
     */
    @Override
    public String toString() {
        return "EnergyMessage{" +
                "type='" + type + '\'' +
                ", association='" + association + '\'' +
                ", kwh=" + kwh +
                ", datetime=" + datetime +
                '}';
    }
}
