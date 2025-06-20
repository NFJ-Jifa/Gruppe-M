package com.gruppem.usage;

import java.time.Instant;

/**
 * DTO for sending energy production/usage messages via RabbitMQ.
 */
public class EnergyMessage {

    private String type;         // "PRODUCER" or "USER"
    private String association;  // "COMMUNITY" or "GRID"
    private double kwh;          // kWh value
    private Instant datetime;    // timestamp of the message

    /** Default constructor for deserialization */
    public EnergyMessage() {
    }

    /** All-args constructor */
    public EnergyMessage(String type, String association, double kwh, Instant datetime) {
        this.type = type;
        this.association = association;
        this.kwh = kwh;
        this.datetime = datetime;
    }

    // ======== Getters ========
    public String getType() {
        return type;
    }

    public String getAssociation() {
        return association;
    }

    public double getKwh() {
        return kwh;
    }

    public Instant getDatetime() {
        return datetime;
    }

    // ======== Setters ========
    public void setType(String type) {
        this.type = type;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public void setKwh(double kwh) {
        this.kwh = kwh;
    }

    public void setDatetime(Instant datetime) {
        this.datetime = datetime;
    }

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
