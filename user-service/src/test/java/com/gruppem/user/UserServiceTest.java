package com.gruppem.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalTime;

import com.gruppem.user.EnergyMessage;
import com.gruppem.user.UserApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class UserServiceTest {

    private UserApplication app;
    private RabbitTemplate rabbit;
    private final String queueName = "energy.messages";

    @BeforeEach
    void setUp() {
        // 1. Мокируем RabbitTemplate
        rabbit = mock(RabbitTemplate.class);
        // 2. Создаём приложение вручную, передаём наш мок и имя очереди
        app = new UserApplication(rabbit, queueName);
        // 3. Настраиваем "конфигурацию" для теста
        app.setType("USER");
        app.setAssociation("COMMUNITY");
        // фиксируем «сейчас» для деталей
        app.setNowSupplier(() -> Instant.parse("2025-06-22T08:00:00Z"));
        app.setTimeSupplier(() -> LocalTime.of(8, 0));
    }

    @Test
    void produceUsage_inPeakHours_addsPeakBonus() {
        app.produceUsage();

        ArgumentCaptor<EnergyMessage> cap = ArgumentCaptor.forClass(EnergyMessage.class);
        verify(rabbit).convertAndSend(eq(queueName), cap.capture());
        EnergyMessage sent = cap.getValue();

        // тип/ассоциация/время
        assertThat(sent.getType()).isEqualTo("USER");
        assertThat(sent.getAssociation()).isEqualTo("COMMUNITY");
        assertThat(sent.getDatetime())
                .isEqualTo(Instant.parse("2025-06-22T08:00:00Z"));

        // kWh ∈ [1;6)
        assertThat(sent.getKwh())
                .isGreaterThanOrEqualTo(1.0)
                .isLessThan(6.0);
    }


    @Test
    void produceUsage_offPeak_onlyBase() {
        // перенастраиваем "сейчас" на непик
        app.setNowSupplier(() -> Instant.parse("2025-06-22T03:15:00Z"));
        app.setTimeSupplier(() -> LocalTime.of(3, 15));

        app.produceUsage();

        ArgumentCaptor<EnergyMessage> cap = ArgumentCaptor.forClass(EnergyMessage.class);
        verify(rabbit, times(1))
                .convertAndSend(eq(queueName), cap.capture());

        EnergyMessage sent = cap.getValue();
        // datetime тот, что подменили
        assertThat(sent.getDatetime())
                .isEqualTo(Instant.parse("2025-06-22T03:15:00Z"));
        // только базовое ∈ [0,2)
        assertThat(sent.getKwh()).isBetween(0.0, 2.0);
    }
}
