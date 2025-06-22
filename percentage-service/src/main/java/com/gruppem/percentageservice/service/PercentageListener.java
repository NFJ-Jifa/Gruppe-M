package com.gruppem.percentageservice.service;

import com.gruppem.percentageservice.dto.EnergyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class PercentageListener {
    private static final Logger log = LoggerFactory.getLogger(PercentageListener.class);

    @RabbitListener(queues = "${energy.raw-queue}")
    public void onEnergyMessage(EnergyMessage msg) {
        log.info("Raw message received in percentage-service: {}", msg);
    }
}
