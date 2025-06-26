package com.gruppeM.energy_rest_api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * This model class represents a time interval (range) with a start and end timestamp.
 * It is used to indicate the available historical data range for energy usage.
 */
public class AvailableRange {

    /**
     * Start of the available range (inclusive).
     * Serialized as ISO 8601 string in JSON.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant from;

    /**
     * End of the available range (inclusive).
     * Also serialized as ISO 8601 string in JSON.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant to;

    /**
     * Default constructor required for deserialization.
     */
    public AvailableRange() { }

    /**
     * Constructor to manually create a range object with start and end values.
     *
     * @param from start of the time range
     * @param to   end of the time range
     */
    public AvailableRange(Instant from, Instant to) {
        this.from = from;
        this.to   = to;
    }

    // === Getters and Setters ===

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
