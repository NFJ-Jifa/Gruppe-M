package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import com.gruppeM.energy_rest_api.service.EnergyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyService energyService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final HourlyUsageRepository hourlyRepo;
    private final String publishQueueName;

    public EnergyController(EnergyService energyService,
                            org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate,
                            HourlyUsageRepository hourlyRepo,
                            @Value("${energy.input-queue:energy.input}") String publishQueueName) {
        this.energyService    = energyService;
        this.rabbitTemplate   = rabbitTemplate;
        this.hourlyRepo       = hourlyRepo;
        this.publishQueueName = publishQueueName;
    }

    /** 1) Текущее */
    @GetMapping("/current")
    public EnergyData getCurrent() {
        return energyService.getCurrentEnergyStatus();
    }

    /** 2) История по Instant */
    @GetMapping("/historical")
    public ResponseEntity<List<EnergyData>> getHistorical(
            @RequestParam("start") Instant start,
            @RequestParam("end")   Instant end) {

        List<EnergyData> list = energyService.getHistoricalEnergyData(start, end);
        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    /** 3) Эндпойнт для публикации */
    @PostMapping("/publish")
    public ResponseEntity<Void> publish(@RequestBody EnergyData data) {
        rabbitTemplate.convertAndSend(publishQueueName, data);
        return ResponseEntity.ok().build();
    }

    /** Геттер для тестов */
    public String getPublishQueueName() {
        return publishQueueName;
    }

    /**
     * 4) Новый эндпойнт: отдаёт фактический диапазон в базе.
     *    Вернёт JSON {"from":"…Z","to":"…Z"}
     */
    @GetMapping("/available-range")
    public AvailableRange availableRange() {
        Instant min = hourlyRepo.findAll().stream()
                .map(HourlyUsage::getHourKey)
                .min(Comparator.naturalOrder())
                .orElse(Instant.EPOCH);
        Instant max = hourlyRepo.findAll().stream()
                .map(HourlyUsage::getHourKey)
                .max(Comparator.naturalOrder())
                .orElse(Instant.EPOCH);
        return new AvailableRange(min, max);
    }
}
