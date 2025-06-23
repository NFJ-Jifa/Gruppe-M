package com.gruppeM.producer;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class ProducerServiceTest {

    private ProducerApplication app;
    private RabbitTemplate rabbit;
    private final String queueName   = "energy.input";
    private final String type        = "PRODUCER";
    private final String association = "COMMUNITY";

    @BeforeEach
    void setUp() {
        rabbit = mock(RabbitTemplate.class);
        app = new ProducerApplication(rabbit, queueName, type, association);
    }

    @Test
    void produce_sendsProducerMessage_withCorrectTypeAndAssociation() {
        app.produce();

        ArgumentCaptor<EnergyMessage> cap = ArgumentCaptor.forClass(EnergyMessage.class);
        verify(rabbit, times(1))
                .convertAndSend(eq(queueName), cap.capture());

        EnergyMessage sent = cap.getValue();
        assertThat(sent.getType()).isEqualTo(type);
        assertThat(sent.getAssociation()).isEqualTo(association);
        assertThat(sent.getDatetime()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    void produce_kwhInExpectedRange() {
        for (int i = 0; i < 20; i++) {
            reset(rabbit);
            app.produce();

            ArgumentCaptor<EnergyMessage> cap = ArgumentCaptor.forClass(EnergyMessage.class);
            verify(rabbit).convertAndSend(eq(queueName), cap.capture());

            double kwh = cap.getValue().getKwh();
            assertThat(kwh)
                    .isGreaterThanOrEqualTo(0.0)
                    .isLessThan(2.0);
        }
    }
}
