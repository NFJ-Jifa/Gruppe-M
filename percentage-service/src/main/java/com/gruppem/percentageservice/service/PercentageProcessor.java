package com.gruppem.percentageservice.service;

import com.gruppem.percentageservice.config.EnergyProperties;
import com.gruppem.percentageservice.model.HourlyUsageMessage;
import com.gruppem.percentageservice.model.PercentageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PercentageProcessor {

    private static final Logger log = LoggerFactory.getLogger(PercentageProcessor.class);
    private final RabbitTemplate rabbit;
    private final EnergyProperties props;

    public PercentageProcessor(RabbitTemplate rabbit,
                               EnergyProperties props) {
        this.rabbit = rabbit;
        this.props  = props;
    }

    @RabbitListener(queues = "${energy.update-queue}")
    public void onUsageUpdate(HourlyUsageMessage msg) {
        double prod = msg.getCommunityProduced();
        double used = msg.getCommunityUsed();
        double grid = msg.getGridUsed();

        double communityDepleted = prod == 0.0
                ? 100.0
                : Math.min(100.0, (used / prod) * 100.0);

        double gridPortion = prod == 0.0
                ? 0.0
                : (grid / (prod + grid)) * 100.0;

        PercentageData out = new PercentageData(msg.getHourKey(), communityDepleted, gridPortion);

        log.info("✔ percentage-service публикует PercentageData: hourKey={}, communityDepleted={}, gridPortion={}",
                out.getHourKey(), out.getCommunityDepleted(), out.getGridPortion());

        rabbit.convertAndSend(props.getFinalQueue(), out);
    }
}
