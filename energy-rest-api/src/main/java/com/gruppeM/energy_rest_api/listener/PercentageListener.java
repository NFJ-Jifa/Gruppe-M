package com.gruppeM.energy_rest_api.listener;

import com.gruppeM.energy_rest_api.dto.PercentageData;
import com.gruppeM.energy_rest_api.model.CurrentPercentage;
import com.gruppeM.energy_rest_api.repository.CurrentPercentageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * This listener receives percentage-related energy data via RabbitMQ.
 * The data typically comes from a backend service that calculates hourly percentages.
 */
@Component
public class PercentageListener {

    private static final Logger log = LoggerFactory.getLogger(PercentageListener.class);
    private final CurrentPercentageRepository repo;

    /**
     * Constructor-based injection of the repository that stores the latest percentage values.
     */
    public PercentageListener(CurrentPercentageRepository repo) {
        this.repo = repo;
    }

    /**
     * This method is triggered when a new PercentageData message is received from the percentage queue.
     * It saves the percentage values to the database as a new CurrentPercentage entry.
     *
     * @param pd The received PercentageData object (via RabbitMQ)
     */
    @RabbitListener(queues = "${energy.percentage-queue}")
    public void onPercentage(PercentageData pd) {
        // Log the received data to the console
        log.info("<<< REST-API received PercentageData: hourKey={}, communityDepleted={}, gridPortion={}",
                pd.getHourKey(), pd.getCommunityDepleted(), pd.getGridPortion());

        // Convert DTO to entity and persist it
        CurrentPercentage cp = new CurrentPercentage(
                pd.getHourKey(),
                pd.getCommunityDepleted(),
                pd.getGridPortion()
        );
        repo.save(cp);
    }
}
