package com.gruppem.usage;

import com.gruppem.usage.EnergyMessage;
import com.gruppem.usage.HourlyUsage;
import com.gruppem.usage.HourlyUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UsageService {
    private static final Logger log = LoggerFactory.getLogger(UsageService.class);

    private final HourlyUsageRepository repo;
    private final RabbitTemplate rabbit;
    private final String updateQueue;

    public UsageService(HourlyUsageRepository repo,
                        RabbitTemplate rabbit,
                        @Value("${energy.update-queue}") String updateQueue) {
        this.repo = repo;
        this.rabbit = rabbit;
        this.updateQueue = updateQueue;
    }

    @RabbitListener(queues = "${energy.input-queue}")
    @Transactional
    public void onMessage(EnergyMessage msg) {
        Instant hourKey = msg.getDatetime().truncatedTo(ChronoUnit.HOURS);

        HourlyUsage usage = repo.findById(hourKey)
                .orElse(new HourlyUsage(hourKey));

        if ("PRODUCER".equals(msg.getType())) {
            usage.setCommunityProduced(usage.getCommunityProduced() + msg.getKwh());
        } else if ("USER".equals(msg.getType())) {
            double available = usage.getCommunityProduced() - usage.getCommunityUsed();
            if (msg.getKwh() <= available) {
                usage.setCommunityUsed(usage.getCommunityUsed() + msg.getKwh());
            } else {
                // part taken from community
                usage.setCommunityUsed(usage.getCommunityUsed() + available);
                // remainder from grid
                double gridDelta = msg.getKwh() - available;
                usage.setGridUsed(usage.getGridUsed() + gridDelta);
                log.info("Hour {}: gridUsed increased by {} kWh (available was {})", hourKey, gridDelta, available);
            }
        }

        repo.save(usage);
        log.debug("Saved HourlyUsage [{}]: produced={}, used={}, gridUsed={}",
                hourKey,
                usage.getCommunityProduced(),
                usage.getCommunityUsed(),
                usage.getGridUsed());
        rabbit.convertAndSend(updateQueue, usage);
    }
}