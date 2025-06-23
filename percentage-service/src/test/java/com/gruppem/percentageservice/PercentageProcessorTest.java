package com.gruppem.percentageservice;

import com.gruppem.percentageservice.config.EnergyProperties;
import com.gruppem.percentageservice.model.HourlyUsageMessage;
import com.gruppem.percentageservice.model.PercentageData;
import com.gruppem.percentageservice.service.PercentageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.gruppem.percentageservice.service.PercentageProcessor;
import com.gruppem.percentageservice.model.PercentageData;
import com.gruppem.percentageservice.model.HourlyUsageMessage;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PercentageProcessorTest {

    private RabbitTemplate rabbit;
    private EnergyProperties props;
    private PercentageProcessor processor;

    @BeforeEach
    void setUp() {
        rabbit = mock(RabbitTemplate.class);
        props  = mock(EnergyProperties.class);

        when(props.getFinalQueue()).thenReturn("energy.percentage");

        processor = new PercentageProcessor(rabbit, props);
    }

    @Test
    void whenNoCommunityProduction_thenCommunityDepleted100_gridPortion0() {
        HourlyUsageMessage msg = new HourlyUsageMessage();
        msg.setHourKey(Instant.parse("2025-06-22T10:00:00Z"));
        msg.setCommunityProduced(0.0);
        msg.setCommunityUsed(0.0);
        msg.setGridUsed(5.0);

        processor.onUsageUpdate(msg);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<PercentageData> cap = ArgumentCaptor.forClass(PercentageData.class);
        verify(rabbit).convertAndSend(eq("energy.percentage"), cap.capture());

        PercentageData out = cap.getValue();

        assertThat(out.getHourKey())
                .isEqualTo(Instant.parse("2025-06-22T10:00:00Z"));

        // при prod==0 communityDepleted=100, gridPortion=0
        assertThat(out.getCommunityDepleted()).isEqualTo(100.0);
        assertThat(out.getGridPortion()).isEqualTo(0.0);
    }

    @Test
    void computesBothPortionsCorrectly() {
        // prod=10, used=3 => communityDepleted = used/prod*100 = 30%
        // grid=2        => gridPortion = grid/(prod+grid)*100 = 2/12*100 ≈ 16.6667%
        HourlyUsageMessage msg = new HourlyUsageMessage();
        msg.setHourKey(Instant.parse("2025-06-22T11:00:00Z"));
        msg.setCommunityProduced(10.0);
        msg.setCommunityUsed(3.0);
        msg.setGridUsed(2.0);

        processor.onUsageUpdate(msg);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<PercentageData> cap = ArgumentCaptor.forClass(PercentageData.class);
        verify(rabbit).convertAndSend(eq("energy.percentage"), cap.capture());

        PercentageData out = cap.getValue();
        assertThat(out.getHourKey())
                .isEqualTo(Instant.parse("2025-06-22T11:00:00Z"));

        // округляем до 4 знаков, чтобы не было плавающих неточностей
        double depleted = Math.round(out.getCommunityDepleted() * 10000.0) / 10000.0;
        double gridPct  = Math.round(out.getGridPortion()      * 10000.0) / 10000.0;

        assertThat(depleted).isEqualTo(30.0);
        assertThat(gridPct ).isEqualTo(16.6667);
    }
}
