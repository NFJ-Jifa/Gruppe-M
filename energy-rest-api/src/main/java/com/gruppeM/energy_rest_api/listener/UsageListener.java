package com.gruppeM.energy_rest_api.listener;

import com.gruppeM.energy_rest_api.dto.UsageMessageDto;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class UsageListener {
    private static final Logger log = LoggerFactory.getLogger(UsageListener.class);
    private final HourlyUsageRepository repo;

    public UsageListener(HourlyUsageRepository repo) {
        this.repo = repo;
    }

    @RabbitListener(queues = "${energy.update-queue}")
    public void onUsage(UsageMessageDto msg) {
        log.info("REST-API получил UsageMessage: {}", msg);

        HourlyUsage usage = new HourlyUsage(msg.getHourKey());
        usage.setCommunityProduced(msg.getCommunityProduced());
        usage.setCommunityUsed(msg.getCommunityUsed());
        usage.setGridUsed(msg.getGridUsed());

        repo.save(usage);
        log.info("Сохранили HourlyUsage for {} => produced={}, used={}, grid={}",
                msg.getHourKey(),
                msg.getCommunityProduced(),
                msg.getCommunityUsed(),
                msg.getGridUsed());
    }
}
