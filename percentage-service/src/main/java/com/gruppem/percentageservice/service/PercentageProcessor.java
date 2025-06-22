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
        log.info("⟳ percentage-service получил HourlyUsageMessage: hourKey={}, prod={}, used={}, grid={}",
                msg.getHourKey(), msg.getCommunityProduced(), msg.getCommunityUsed(), msg.getGridUsed());

        double prod = msg.getCommunityProduced();
        double used = msg.getCommunityUsed();
        double grid = msg.getGridUsed();

        // Рассчитываем долю grid
        double gridPortion = (prod + grid) == 0.0
                ? 0.0
                : (grid / (prod + grid)) * 100.0;

        // Логируем результат до создания DTO
        log.info("✔ percentage-service публикует PercentageData: hourKey={}, gridPortion={}",
                msg.getHourKey(), gridPortion);

        // Создаём DTO и отправляем дальше
        PercentageData out = new PercentageData(msg.getHourKey(), gridPortion);
        rabbit.convertAndSend(props.getFinalQueue(), out);
    }
}
