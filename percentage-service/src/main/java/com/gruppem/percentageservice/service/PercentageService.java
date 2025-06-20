
package com.gruppem.percentageservice.service;

import com.gruppem.percentageservice.config.EnergyProperties;
import com.gruppem.percentageservice.dto.EnergyMessage;
import com.gruppem.percentageservice.model.EnergyData;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PercentageService {

    private final RabbitTemplate rabbit;
    private final EnergyProperties props;

    public PercentageService(RabbitTemplate rabbit,
                             EnergyProperties props) {
        this.rabbit = rabbit;
        this.props  = props;
    }

    @RabbitListener(queues = "${energy.raw-queue}")
    public void onRaw(EnergyMessage msg) {
        // Ваши формулы расчёта, здесь просто примеры
        double communityDepleted = 100.0 - msg.getKwh();
        double gridPortion      = msg.getKwh() * 0.05;
        EnergyData data = new EnergyData(msg.getDatetime(), communityDepleted, gridPortion);

        // шлём дальше в очередь, заданную в application.yml → energy.update-queue
        rabbit.convertAndSend(props.getUpdateQueue(), data);
    }
}
