package com.gruppeM.energy_rest_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnergyMessage {

    private String type;
    private String association;
    private double kwh;
    private Instant datetime;

    public EnergyMessage() { }

    public EnergyMessage(String type, String association, double kwh, Instant datetime) {
        this.type        = type;
        this.association = association;
        this.kwh         = kwh;
        this.datetime    = datetime;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getAssociation() {
        return association;
    }
    public void setAssociation(String association) {
        this.association = association;
    }

    public double getKwh() {
        return kwh;
    }
    public void setKwh(double kwh) {
        this.kwh = kwh;
    }

    public Instant getDatetime() {
        return datetime;
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
