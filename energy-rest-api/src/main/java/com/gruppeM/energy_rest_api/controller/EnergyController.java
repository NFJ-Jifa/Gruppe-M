package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.service.EnergyService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyService energyService;
    private final RabbitTemplate rabbit;
    private final String inputQueue;

    public EnergyController(EnergyService energyService,
                            RabbitTemplate rabbit,
                            @Value("${energy.input-queue}") String inputQueue) {
        this.energyService = energyService;
        this.rabbit = rabbit;
        this.inputQueue = inputQueue;
    }

    @GetMapping("/current")
    public EnergyData getCurrent() {
        return energyService.getCurrentEnergyStatus();
    }

    @GetMapping("/historical")
    public List<EnergyData> getHistorical(
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) {
        // если нужно, парсим LocalDateTime здесь
        return energyService
                .getHistoricalEnergyData(
                        java.time.LocalDateTime.parse(start),
                        java.time.LocalDateTime.parse(end)
                );
    }

    @PostMapping("/publish")
    public void publish(@RequestBody EnergyData data) {
        rabbit.convertAndSend(inputQueue, data);
    }
}
