package com.gruppem.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

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
        rabbit = mock(RabbitTemplate.class);
        app = new UserApplication(rabbit, queueName);
        app.setType("USER");
        app.setAssociation("COMMUNITY");
        app.setNowSupplier(() -> Instant.parse("2025-06-22T08:00:00Z"));
        app.setTimeSupplier(() -> LocalTime.of(8, 0));
    }

    @Test
    void produceUsage_inPeakHours_sendsBothCommunityAndGrid() {
        app.produceUsage();

        // Ловим все вызовы
        ArgumentCaptor<EnergyMessage> cap = ArgumentCaptor.forClass(EnergyMessage.class);
        verify(rabbit, atLeast(1)).convertAndSend(eq(queueName), cap.capture());

        List<EnergyMessage> sentMessages = cap.getAllValues();
        // Ожидаем ровно два сообщения: COMMUNITY и GRID
        assertThat(sentMessages).hasSize(2);

        // Проверяем, что присутствуют оба типа ассоциаций
        boolean hasCommunity = sentMessages.stream()
                .anyMatch(m -> "COMMUNITY".equals(m.getAssociation()));
        boolean hasGrid = sentMessages.stream()
                .anyMatch(m -> "GRID".equals(m.getAssociation()));
        assertThat(hasCommunity).isTrue();
        assertThat(hasGrid).isTrue();

        // Общие проверки для каждого сообщения
        for (EnergyMessage msg : sentMessages) {
            assertThat(msg.getType()).isEqualTo("USER");
            assertThat(msg.getDatetime())
                    .isEqualTo(Instant.parse("2025-06-22T08:00:00Z"));
        }

        // Проверяем диапазоны kWh:
        // - COMMUNITY ≤ 2.0
        // - GRID ≥ 0
        EnergyMessage communityMsg = sentMessages.stream()
                .filter(m -> "COMMUNITY".equals(m.getAssociation()))
                .findFirst().orElseThrow();
        EnergyMessage gridMsg = sentMessages.stream()
                .filter(m -> "GRID".equals(m.getAssociation()))
                .findFirst().orElseThrow();

        assertThat(communityMsg.getKwh())
                .isGreaterThanOrEqualTo(0.0)
                .isLessThanOrEqualTo(2.0);

        assertThat(gridMsg.getKwh())
                .isGreaterThanOrEqualTo(0.0)
                .isLessThan(6.0);  // общее потребление <6 кВт·ч, значит grid <6
    }

    @Test
    void produceUsage_offPeak_sendsCommunityAndZeroGrid() {
        // Перенастраиваем время на непиковое
        app.setNowSupplier(() -> Instant.parse("2025-06-22T03:15:00Z"));
        app.setTimeSupplier(() -> LocalTime.of(3, 15));

        app.produceUsage();

        // Ловим оба вызова
        ArgumentCaptor<EnergyMessage> cap = ArgumentCaptor.forClass(EnergyMessage.class);
        verify(rabbit, times(2)).convertAndSend(eq(queueName), cap.capture());

        List<EnergyMessage> sentMessages = cap.getAllValues();
        // Ожидаем ровно два сообщения
        assertThat(sentMessages).hasSize(2);

        // Находим сообщения по ассоциации
        EnergyMessage communityMsg = sentMessages.stream()
                .filter(m -> "COMMUNITY".equals(m.getAssociation()))
                .findFirst().orElseThrow();
        EnergyMessage gridMsg = sentMessages.stream()
                .filter(m -> "GRID".equals(m.getAssociation()))
                .findFirst().orElseThrow();

        // Проверяем общее
        assertThat(communityMsg.getType()).isEqualTo("USER");
        assertThat(gridMsg.getType()).isEqualTo("USER");
        assertThat(communityMsg.getDatetime())
                .isEqualTo(Instant.parse("2025-06-22T03:15:00Z"));
        assertThat(gridMsg.getDatetime())
                .isEqualTo(Instant.parse("2025-06-22T03:15:00Z"));

        // В непиковые часы базовое потребление ∈ [0,2), community = base, а grid = 0
        assertThat(communityMsg.getKwh())
                .isGreaterThanOrEqualTo(0.0)
                .isLessThan(2.0);
        assertThat(gridMsg.getKwh())
                .isEqualTo(0.0);
    }

}
