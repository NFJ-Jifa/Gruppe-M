package com.gruppeM.energy_rest_api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

public class AvailableRange {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant from;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant to;

    public AvailableRange() { }

    public AvailableRange(Instant from, Instant to) {
        this.from = from;
        this.to   = to;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }
}
