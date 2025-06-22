package com.gruppeM.energy_rest_api.service;

import com.gruppeM.energy_rest_api.model.CurrentPercentage;
import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.CurrentPercentageRepository;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnergyService {

    private final CurrentPercentageRepository currentRepo;
    private final HourlyUsageRepository hourlyRepo;
    private final RabbitTemplate rabbitTemplate;
    private final String inputQueue;

    public EnergyService(CurrentPercentageRepository currentRepo,
                         HourlyUsageRepository hourlyRepo,
                         RabbitTemplate rabbitTemplate,
                         @Value("${energy.input-queue:energy.input}") String inputQueue) {
        this.currentRepo    = currentRepo;
        this.hourlyRepo     = hourlyRepo;
        this.rabbitTemplate = rabbitTemplate;
        this.inputQueue     = inputQueue;
    }

    /** Текущий статус */
    public EnergyData getCurrentEnergyStatus() {
        Instant nowHour = Instant.now().truncatedTo(ChronoUnit.HOURS);
        CurrentPercentage cp = currentRepo.findById(nowHour)
                .orElse(new CurrentPercentage(nowHour, 0.0, 0.0));

        // Используем getHourKey() вместо несуществующего getHour()
        LocalDateTime dt = LocalDateTime.ofInstant(cp.getHourKey(), ZoneOffset.UTC);
        return new EnergyData(dt, cp.getCommunityDepleted(), cp.getGridPortion());
    }

    /** Исторические данные по Instant */
    public List<EnergyData> getHistoricalEnergyData(Instant start, Instant end) {
        List<HourlyUsage> usages = hourlyRepo.findAllByHourKeyBetween(
                start.truncatedTo(ChronoUnit.HOURS),
                end.truncatedTo(ChronoUnit.HOURS)
        );
        return usages.stream()
                .map(u -> {
                    double total     = u.getCommunityProduced() + u.getCommunityUsed() + u.getGridUsed();
                    double depleted  = total == 0.0 ? 0.0 : u.getCommunityUsed() / total * 100.0;
                    double gridPct   = total == 0.0 ? 0.0 : u.getGridUsed()     / total * 100.0;
                    LocalDateTime dt = LocalDateTime.ofInstant(u.getHourKey(), ZoneOffset.UTC);
                    return new EnergyData(dt, depleted, gridPct);
                })
                .collect(Collectors.toList());
    }

    /** Публикация */
    public void publish(EnergyData data) {
        rabbitTemplate.convertAndSend(inputQueue, data);
    }
}
