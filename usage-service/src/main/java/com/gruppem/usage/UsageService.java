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

/**
 * Service that processes incoming energy messages and updates usage statistics per hour.
 */
@Service
public class UsageService {
    private static final Logger log = LoggerFactory.getLogger(UsageService.class);

    private final HourlyUsageRepository repo;
    private final RabbitTemplate rabbit;
    private final String updateQueue;

    /**
     * Constructor injection of required components.
     */
    public UsageService(HourlyUsageRepository repo,
                        RabbitTemplate rabbit,
                        @Value("${energy.update-queue}") String updateQueue) {
        this.repo = repo;
        this.rabbit = rabbit;
        this.updateQueue = updateQueue;
    }

    /**
     * Listener for incoming energy messages from RabbitMQ.
     * The method processes producer or consumer messages and updates the hourly usage entity.
     */
    @RabbitListener(queues = "${energy.input-queue}")
    @Transactional
    public void onMessage(EnergyMessage msg) {
        // Round timestamp down to the start of the hour
        Instant hourKey = msg.getDatetime().truncatedTo(ChronoUnit.HOURS);

        // Try to fetch existing hourly record; if not found, create a new one
        HourlyUsage usage = repo.findById(hourKey)
                .orElse(new HourlyUsage(hourKey));

        if ("PRODUCER".equals(msg.getType())) {
            // All produced energy is considered to be from the community
            usage.setCommunityProduced(usage.getCommunityProduced() + msg.getKwh());

        } else if ("USER".equals(msg.getType())) {
            // Handle energy consumption
            switch (msg.getAssociation()) {

                case "COMMUNITY" -> {
                    // First try to satisfy usage from available community energy
                    double available = usage.getCommunityProduced() - usage.getCommunityUsed();

                    if (msg.getKwh() <= available) {
                        // All energy can be served by community production
                        usage.setCommunityUsed(usage.getCommunityUsed() + msg.getKwh());
                    } else {
                        // Partial energy from community
                        usage.setCommunityUsed(usage.getCommunityUsed() + available);

                        // Remainder must be taken from the grid
                        double gridDelta = msg.getKwh() - available;
                        usage.setGridUsed(usage.getGridUsed() + gridDelta);

                        log.info("Hour {}: split USER-COMMUNITY {} into community={}, grid={}",
                                hourKey, msg.getKwh(), available, gridDelta);
                    }
                }

                case "GRID" -> {
                    // All energy directly taken from the grid
                    usage.setGridUsed(usage.getGridUsed() + msg.getKwh());
                }

                default -> {
                    // Fallback for unknown or malformed association field
                    double available = usage.getCommunityProduced() - usage.getCommunityUsed();
                    double communityPart = Math.min(available, msg.getKwh());
                    double gridPart = msg.getKwh() - communityPart;

                    usage.setCommunityUsed(usage.getCommunityUsed() + communityPart);
                    usage.setGridUsed(usage.getGridUsed() + gridPart);

                    log.warn("Unknown association '{}', split {} into community={}, grid={}",
                            msg.getAssociation(), msg.getKwh(), communityPart, gridPart);
                }
            }

        } else {
            // Unknown message type (not PRODUCER or USER)
            log.warn("Unknown message type '{}'", msg.getType());
        }

        // Save the updated usage data to the database
        repo.save(usage);

        // Send the updated usage data to another queue (for UI update, logging, etc.)
        rabbit.convertAndSend(updateQueue, usage);

        // Log the final values for this hour
        log.debug("Hour {}: produced={} used={} grid={}",
                hourKey,
                usage.getCommunityProduced(),
                usage.getCommunityUsed(),
                usage.getGridUsed());
    }
}
