
package com.gruppem.percentageservice.service;

import com.gruppem.percentageservice.config.EnergyProperties;
import com.gruppem.percentageservice.model.HourlyUsageMessage;
import com.gruppem.percentageservice.model.PercentageData;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PercentageProcessor {

    private final RabbitTemplate rabbit;
    private final EnergyProperties props;

    public PercentageProcessor(RabbitTemplate rabbit,
                               EnergyProperties props) {
        this.rabbit = rabbit;
        this.props  = props;
    }

    @RabbitListener(queues = "${energy.update-queue}")
    public void onUsageUpdate(HourlyUsageMessage msg) {
        double total = msg.getCommunityProduced()
                + msg.getCommunityUsed()
                + msg.getGridUsed();
        double pctGrid = total == 0.0
                ? 0.0
                : (msg.getGridUsed() / total) * 100.0;

        PercentageData out = new PercentageData(msg.getHourKey(), pctGrid);
        rabbit.convertAndSend(props.getFinalQueue(), out);
    }
}
