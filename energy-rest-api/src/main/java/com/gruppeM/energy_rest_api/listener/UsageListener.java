package com.gruppeM.energy_rest_api.listener;

import com.gruppeM.energy_rest_api.dto.UsageMessageDto;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * This listener handles incoming hourly energy usage data from RabbitMQ.
 * It receives messages from the update queue and stores them in the database.
 */
@Component
public class UsageListener {

    private static final Logger log = LoggerFactory.getLogger(UsageListener.class);
    private final HourlyUsageRepository repo;

    /**
     * Constructor-based dependency injection of the repository
     * responsible for storing hourly usage values.
     */
    public UsageListener(HourlyUsageRepository repo) {
        this.repo = repo;
    }

    /**
     * Handles incoming UsageMessageDto from the configured RabbitMQ queue.
     * This method stores the data in the database as a HourlyUsage entity.
     *
     * @param msg The received usage data (produced/used/grid energy per hour)
     */
    @RabbitListener(queues = "${energy.update-queue}")
    public void onUsage(UsageMessageDto msg) {
        log.info("REST-API received UsageMessage: {}", msg);

        // Map the incoming DTO to the JPA entity
        HourlyUsage usage = new HourlyUsage(msg.getHourKey());
        usage.setCommunityProduced(msg.getCommunityProduced());
        usage.setCommunityUsed(msg.getCommunityUsed());
        usage.setGridUsed(msg.getGridUsed());

        // Save the new record in the database
        repo.save(usage);

        log.info("Saved HourlyUsage for {} => produced={}, used={}, grid={}",
                msg.getHourKey(),
                msg.getCommunityProduced(),
                msg.getCommunityUsed(),
                msg.getGridUsed());
    }
}
