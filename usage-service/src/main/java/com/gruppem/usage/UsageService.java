package com.gruppem.usage;

import com.gruppem.usage.EnergyMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Listens to incoming EnergyMessage, updates the hourly aggregates
 * and publishes updated HourlyUsage to the next queue.
 */
@Service
public class UsageService {

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
        // Round timestamp down to the hour
        Instant hourKey = msg.getDatetime().truncatedTo(ChronoUnit.HOURS);

        // Load or create HourlyUsage
        HourlyUsage usage = repo.findById(hourKey)
                .orElse(new HourlyUsage(hourKey));

        // Update produced vs used vs grid
        if ("PRODUCER".equals(msg.getType())) {
            usage.setCommunityProduced(usage.getCommunityProduced() + msg.getKwh());
        } else if ("USER".equals(msg.getType())) {
            double available = usage.getCommunityProduced() - usage.getCommunityUsed();
            if (msg.getKwh() <= available) {
                usage.setCommunityUsed(usage.getCommunityUsed() + msg.getKwh());
            } else {
                // community pool depleted, rest from grid
                usage.setCommunityUsed(usage.getCommunityUsed() + available);
                usage.setGridUsed(usage.getGridUsed() + (msg.getKwh() - available));
            }
        }

        // Persist changes
        repo.save(usage);

        // Publish aggregated update
        rabbit.convertAndSend(updateQueue, usage);
    }
}
