package com.gruppem.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO for sending messages to RabbitMQ.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
public class EnergyMessage {
    private String type;         // "PRODUCER" or "USER"
    private String association;  // "COMMUNITY" or "GRID"
    private double kwh;          // Energy usage/production in kilowatt-hours
    private Instant datetime;    // Timestamp of the message

    public EnergyMessage() {}

    public EnergyMessage(String type, String association, double kwh, Instant datetime) {
        this.type = type;
        this.association = association;
        this.kwh = kwh;
        this.datetime = datetime;
    }

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
