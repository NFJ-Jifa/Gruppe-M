package com.gruppeM.energy_rest_api.service;

import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnergyService {

    private final HourlyUsageRepository repo;

    public EnergyService(HourlyUsageRepository repo) {
        this.repo = repo;
    }

    /**
     * Возвращаем самый последний час и вычисляем проценты.
     */
    public EnergyData getCurrentEnergyStatus() {
        Optional<HourlyUsage> opt = repo.findAll()
                .stream()
                .max(Comparator.comparing(HourlyUsage::getHourKey));

        HourlyUsage hu = opt.orElseThrow(() ->
                new IllegalStateException("Нет данных HourlyUsage")
        );

        double prod = hu.getCommunityProduced();
        double used = hu.getCommunityUsed();
        double grid = hu.getGridUsed();

        double communityDepleted = prod == 0.0
                ? 100.0
                : Math.min(100.0, (used / prod) * 100.0);
        double gridPortion = (prod + grid) == 0.0
                ? 0.0
                : (grid / (prod + grid)) * 100.0;

        // Здесь Instant — ключ часа
        return new EnergyData(hu.getHourKey(), communityDepleted, gridPortion);
    }

    /**
     * Исторические данные за [start, end], включая граничные часы.
     */
    public List<EnergyData> getHistoricalEnergyData(Instant start, Instant end) {
        return repo.findAll()
                .stream()
                .filter(hu -> !hu.getHourKey().isBefore(start) && !hu.getHourKey().isAfter(end))
                .map(hu -> {
                    double prod = hu.getCommunityProduced();
                    double used = hu.getCommunityUsed();
                    double grid = hu.getGridUsed();

                    double communityDepleted = prod == 0.0
                            ? 100.0
                            : Math.min(100.0, (used / prod) * 100.0);
                    double gridPortion = (prod + grid) == 0.0
                            ? 0.0
                            : (grid / (prod + grid)) * 100.0;

                    return new EnergyData(hu.getHourKey(), communityDepleted, gridPortion);
                })
                // Сортируем по времени по возрастанию
                .sorted(Comparator.comparing(EnergyData::getHour))
                .collect(Collectors.toList());
    }
}
