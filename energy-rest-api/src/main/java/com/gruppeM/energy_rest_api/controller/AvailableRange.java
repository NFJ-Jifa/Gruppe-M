package com.gruppeM.energy_rest_api.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;

/**
 * This class represents a time range between two Instants (timestamps).
 * It is used to communicate the available time span of historical energy data.
 */
public class AvailableRange {

    /**
     * Start of the available range (formatted as ISO 8601 string).
     * The @JsonFormat annotation ensures correct JSON serialization.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant from;

    /**
     * End of the available range (formatted as ISO 8601 string).
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant to;

    /**
     * Default constructor (required for JSON deserialization).
     */
    public AvailableRange() {}

    /**
     * Constructor to create a range with a given start and end.
     *
     * @param from start timestamp
     * @param to   end timestamp
     */
    public AvailableRange(Instant from, Instant to) {
        this.from = from;
        this.to   = to;
    }

    // Getter and setter for 'from'
    public Instant getFrom() { return from; }
    public void setFrom(Instant from) { this.from = from; }

    // Getter and setter for 'to'
    public Instant getTo() { return to; }
    public void setTo(Instant to) { this.to = to; }
}