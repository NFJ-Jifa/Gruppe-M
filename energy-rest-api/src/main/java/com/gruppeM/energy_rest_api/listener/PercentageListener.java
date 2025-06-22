package com.gruppeM.energy_rest_api.listener;

import com.gruppeM.energy_rest_api.dto.PercentageData;
import com.gruppeM.energy_rest_api.model.CurrentPercentage;
import com.gruppeM.energy_rest_api.repository.CurrentPercentageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PercentageListener {

    private static final Logger log = LoggerFactory.getLogger(PercentageListener.class);

    private final CurrentPercentageRepository repo;

    public PercentageListener(CurrentPercentageRepository repo) {
        this.repo = repo;
    }

    @RabbitListener(queues = "${energy.percentage-queue}")
    public void onPercentage(PercentageData pd) {
        log.info("<<< REST-API получил PercentageData: hourKey={} percentage={}",
                pd.getHourKey(), pd.getPercentage());

        double gridPct = pd.getPercentage();
        double communityDepleted = 100.0 - gridPct;
        repo.save(new CurrentPercentage(pd.getHourKey(), communityDepleted, gridPct));
    }
}
