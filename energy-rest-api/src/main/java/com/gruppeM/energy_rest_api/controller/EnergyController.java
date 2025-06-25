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

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final HourlyUsageRepository hourlyRepo;
    private final EnergyService energyService;
    private final RabbitTemplate rabbit;
    private final String inputQueue;

    public EnergyController(HourlyUsageRepository hourlyRepo,
                            EnergyService energyService,
                            RabbitTemplate rabbit,
                            @Value("${energy.input-queue}") String inputQueue) {
        this.hourlyRepo    = hourlyRepo;
        this.energyService = energyService;
        this.rabbit        = rabbit;
        this.inputQueue    = inputQueue;
    }

    @GetMapping("/current")
    public ResponseEntity<EnergyData> getCurrent() {
        EnergyData current = energyService.getCurrentEnergyStatus();
        return ResponseEntity.ok(current);
    }

    /** Публикация сырых сообщений */
    @PostMapping("/publish")
    public ResponseEntity<Void> publishRaw(@RequestBody EnergyMessage msg) {
        rabbit.convertAndSend(inputQueue, msg);
        return ResponseEntity.accepted().build();
    }

    /** Доступный диапазон */
    @GetMapping("/available-range")
    public ResponseEntity<AvailableRange> availableRange() {
        Optional<Instant> minOpt = hourlyRepo.findMinHourKey();
        Optional<Instant> maxOpt = hourlyRepo.findMaxHourKey();
        if (minOpt.isEmpty() || maxOpt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new AvailableRange(minOpt.get(), maxOpt.get()));
    }

    /** 2) Исторические данные с raw kWh и % */
    @GetMapping("/historical")
    public ResponseEntity<List<HistoricalUsageDto>> getHistorical(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        // Усечём до границ часа
        Instant from = start.truncatedTo(ChronoUnit.HOURS);
        Instant to   = end.truncatedTo(ChronoUnit.HOURS);

        List<HistoricalUsageDto> list = energyService.getHistoricalEnergyData(from, to);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }
}
