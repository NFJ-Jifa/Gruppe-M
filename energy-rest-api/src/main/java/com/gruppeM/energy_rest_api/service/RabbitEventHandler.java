package com.gruppeM.energy_rest_api.service;

import com.gruppeM.energy_rest_api.model.CurrentPercentage;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.CurrentPercentageRepository;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventHandler {

    private final HourlyUsageRepository hourlyRepo;
    private final CurrentPercentageRepository currentRepo;

    public RabbitEventHandler(HourlyUsageRepository hourlyRepo,
                              CurrentPercentageRepository currentRepo) {
        this.hourlyRepo  = hourlyRepo;
        this.currentRepo = currentRepo;
    }



    /**
     * Сохраняем каждый CurrentPercentage, который приходит из очереди energy.percentage
     */
    @RabbitListener(queues = "${energy.percentage-queue:energy.percentage}")
    public void handleCurrentPercentage(CurrentPercentage msg) {
        currentRepo.save(msg);
    }
}
