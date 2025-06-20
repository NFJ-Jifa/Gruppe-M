package com.gruppem.user;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * DTO для отправки сообщений в RabbitMQ.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Getter
@Setter
public class EnergyMessage {
    private String type;         // "PRODUCER" или "USER"
    private String association;  // "COMMUNITY" или "GRID"
    private double kwh;          // kWh
    private Instant datetime;    // timestamp

    public EnergyMessage() {}

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
