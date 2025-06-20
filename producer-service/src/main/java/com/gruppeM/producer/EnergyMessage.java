package com.gruppeM.producer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO for sending energy production/usage messages via RabbitMQ.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
public class EnergyMessage {

    private String type;         // "PRODUCER" or "USER"
    private String association;  // "COMMUNITY" or "GRID"
    private double kwh;          // kWh value
    private Instant datetime;    // timestamp of the message

    // Default constructor for deserialization
    public EnergyMessage() {
    }

    // All-args constructor
    public EnergyMessage(String type, String association, double kwh, Instant datetime) {
        this.type = type;
        this.association = association;
        this.kwh = kwh;
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
