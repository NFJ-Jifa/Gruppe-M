package com.gruppeM.energy_rest_api.service;

import com.gruppeM.energy_rest_api.model.EnergyData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EnergyService {

    public EnergyData getCurrentEnergyStatus() {
        return new EnergyData(LocalDateTime.now(), 100.0, 5.5);
    }

    public List<EnergyData> getHistoricalEnergyData(LocalDateTime start, LocalDateTime end) {
        return List.of(
                new EnergyData(start.plusHours(1), 95.0, 3.2),
                new EnergyData(start.plusHours(2), 97.5, 4.1),
                new EnergyData(start.plusHours(3), 99.0, 2.5)
        );
    }
}
