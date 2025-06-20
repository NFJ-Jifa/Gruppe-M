
package com.gruppem.percentageservice.dto;

import java.time.Instant;

public class EnergyMessage {
    private String type;        // PRODUCER or USER
    private String association; // COMMUNITY or GRID
    private double kwh;
    private Instant datetime;

    public EnergyMessage() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAssociation() { return association; }
    public void setAssociation(String association) { this.association = association; }

    public double getKwh() { return kwh; }
    public void setKwh(double kwh) { this.kwh = kwh; }

    public Instant getDatetime() { return datetime; }
    public void setDatetime(Instant datetime) { this.datetime = datetime; }
}
