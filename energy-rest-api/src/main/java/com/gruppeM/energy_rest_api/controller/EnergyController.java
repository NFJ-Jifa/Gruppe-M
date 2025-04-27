package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.service.EnergyService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping("/current")
    public EnergyData getCurrentEnergyStatus() {
        return energyService.getCurrentEnergyStatus();
    }

    @GetMapping("/historical")
    public List<EnergyData> getHistoricalEnergyData(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return energyService.getHistoricalEnergyData(start, end);
    }
}
