package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.dto.HistoricalUsageDto;
import com.gruppeM.energy_rest_api.dto.EnergyMessage;
import com.gruppeM.energy_rest_api.model.AvailableRange;
import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import com.gruppeM.energy_rest_api.service.EnergyService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * This controller handles all HTTP requests related to energy data.
 * It acts as the bridge between the frontend (GUI) and the backend logic,
 * and also allows publishing new messages to the RabbitMQ queue.
 */
@RestController
@RequestMapping("/energy") // Base URL for all energy-related endpoints
public class EnergyController {

    private final HourlyUsageRepository hourlyRepo;
    private final EnergyService energyService;
    private final RabbitTemplate rabbit;
    private final String inputQueue;

    /**
     * Constructor-based dependency injection.
     *
     * @param hourlyRepo     Repository for querying historical usage data
     * @param energyService  Handles business logic like calculating percentages
     * @param rabbit         Sends messages to RabbitMQ
     * @param inputQueue     Name of the queue for new raw energy messages
     */
    public EnergyController(HourlyUsageRepository hourlyRepo,
                            EnergyService energyService,
                            RabbitTemplate rabbit,
                            @Value("${energy.input-queue}") String inputQueue) {
        this.hourlyRepo    = hourlyRepo;
        this.energyService = energyService;
        this.rabbit        = rabbit;
        this.inputQueue    = inputQueue;
    }

    /**
     * Returns the most recent energy status with calculated percentage values.
     * GET /energy/current
     *
     * @return an EnergyData object representing the current situation
     */
    @GetMapping("/current")
    public ResponseEntity<EnergyData> getCurrent() {
        EnergyData current = energyService.getCurrentEnergyStatus();
        return ResponseEntity.ok(current);
    }

    /**
     * Publishes a raw energy message (from a producer) to RabbitMQ.
     * POST /energy/publish
     *
     * @param msg the energy message to be forwarded to the message queue
     * @return HTTP 202 Accepted
     */
    @PostMapping("/publish")
    public ResponseEntity<Void> publishRaw(@RequestBody EnergyMessage msg) {
        rabbit.convertAndSend(inputQueue, msg);
        return ResponseEntity.accepted().build();
    }

    /**
     * Returns the time span in which energy data is available.
     * GET /energy/available-range
     *
     * @return AvailableRange object (from earliest to latest entry)
     */
    @GetMapping("/available-range")
    public ResponseEntity<AvailableRange> availableRange() {
        Optional<Instant> minOpt = hourlyRepo.findMinHourKey();
        Optional<Instant> maxOpt = hourlyRepo.findMaxHourKey();
        if (minOpt.isEmpty() || maxOpt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new AvailableRange(minOpt.get(), maxOpt.get()));
    }

    /**
     * Returns a list of hourly energy usage values between two timestamps.
     * GET /energy/historical?start=...&end=...
     *
     * @param start Start timestamp (ISO 8601)
     * @param end   End timestamp (ISO 8601)
     * @return List of HistoricalUsageDto entries
     */
    @GetMapping("/historical")
    public ResponseEntity<List<HistoricalUsageDto>> getHistorical(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        // Align timestamps to the start of each hour
        Instant from = start.truncatedTo(ChronoUnit.HOURS);
        Instant to   = end.truncatedTo(ChronoUnit.HOURS);

        List<HistoricalUsageDto> list = energyService.getHistoricalEnergyData(from, to);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }
}
