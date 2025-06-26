package com.gruppeM.energy_rest_api.service;

import com.gruppeM.energy_rest_api.dto.HistoricalUsageDto;
import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This service contains business logic for calculating energy statistics,
 * including current status and historical energy usage data.
 */
@Service
public class EnergyService {

    private final HourlyUsageRepository repo;

    /**
     * Constructor-based dependency injection.
     * The repository is used to access historical usage data.
     */
    public EnergyService(HourlyUsageRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieves the latest hourly energy record from the database and calculates:
     * - how much of the community-produced energy was actually used
     * - how much of the total consumption came from the public grid
     *
     * @return EnergyData containing these percentages for the latest hour
     */
    public EnergyData getCurrentEnergyStatus() {
        // Get the most recent hourly record
        Optional<HourlyUsage> opt = repo.findAll()
                .stream()
                .max(Comparator.comparing(HourlyUsage::getHourKey));

        // Throw an error if no data is found
        HourlyUsage hu = opt.orElseThrow(() ->
                new IllegalStateException("No hourly energy data found.")
        );

        double prod = hu.getCommunityProduced();
        double used = hu.getCommunityUsed();
        double grid = hu.getGridUsed();

        // Calculate % of community energy depletion (how much was actually used)
        double communityDepleted = prod == 0.0
                ? 100.0 // if nothing was produced, treat it as fully depleted
                : Math.min(100.0, (used / prod) * 100.0);

        // Calculate % of energy that came from the public grid
        double gridPortion = (prod + grid) == 0.0
                ? 0.0 // if there was no consumption at all
                : (grid / (prod + grid)) * 100.0;

        return new EnergyData(hu.getHourKey(), communityDepleted, gridPortion);
    }

    /**
     * Retrieves all hourly usage records between two timestamps and
     * maps them to DTOs including calculated statistics.
     *
     * @param start start of the interval (inclusive)
     * @param end   end of the interval (inclusive)
     * @return list of HistoricalUsageDto for each hour in range
     */
    public List<HistoricalUsageDto> getHistoricalEnergyData(Instant start, Instant end) {
        return repo.findAllByHourKeyBetween(start, end)
                .stream()
                .sorted(Comparator.comparing(HourlyUsage::getHourKey)) // sort chronologically
                .map(hu -> {
                    double prod = hu.getCommunityProduced();
                    double used = hu.getCommunityUsed();
                    double grid = hu.getGridUsed();

                    // Same calculations as in current() but per hour
                    double depleted = prod == 0.0
                            ? 100.0
                            : Math.min(100.0, (used / prod) * 100.0);
                    double portion = (prod + grid) == 0.0
                            ? 0.0
                            : (grid / (prod + grid)) * 100.0;

                    return new HistoricalUsageDto(
                            hu.getHourKey(),
                            prod,
                            used,
                            grid,
                            depleted,
                            portion
                    );
                })
                .collect(Collectors.toList());
    }
}
