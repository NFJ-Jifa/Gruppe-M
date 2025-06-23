package com.gruppem.usage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock
    private HourlyUsageRepository repo;

    @Mock
    private RabbitTemplate rabbit;

    private UsageService service;

    private final String updateQueue = "energy.update";

    @BeforeEach
    void setUp() {
        // инициализируем сервис вручную, передав мок-репо, мок-ракет и имя очереди
        service = new UsageService(repo, rabbit, updateQueue);
    }

    @Test
    void onMessage_producerIncrementsCommunityProduced() {
        // given: нет записи для данного часа
        Instant ts = Instant.parse("2025-06-22T10:15:30Z")
                .truncatedTo(ChronoUnit.HOURS);
        when(repo.findById(ts)).thenReturn(Optional.empty());

        EnergyMessage msg = new EnergyMessage("PRODUCER", "COMMUNITY", 5.5, ts.plusSeconds(900));

        // when
        service.onMessage(msg);

        // then: сохранится запись с communityProduced=5.5, communityUsed=0, gridUsed=0
        ArgumentCaptor<HourlyUsage> capt = ArgumentCaptor.forClass(HourlyUsage.class);
        verify(repo).save(capt.capture());
        HourlyUsage saved = capt.getValue();

        assertEquals(ts,              saved.getHourKey());
        assertEquals(5.5,             saved.getCommunityProduced(), 1e-6);
        assertEquals(0.0,             saved.getCommunityUsed(),     1e-6);
        assertEquals(0.0,             saved.getGridUsed(),          1e-6);

        // и будет отправлено в очередь обновлений
        verify(rabbit).convertAndSend(updateQueue, saved);
    }

    @Test
    void onMessage_userExceedsAvailableSplitsBetweenCommunityAndGrid() {
        // given: есть запись с produced=10, used=6, grid=0
        Instant hour = Instant.parse("2025-06-22T11:00:00Z");
        HourlyUsage existing = new HourlyUsage(hour);
        existing.setCommunityProduced(10.0);
        existing.setCommunityUsed(6.0);
        existing.setGridUsed(0.0);
        when(repo.findById(hour)).thenReturn(Optional.of(existing));

        // user запрашивает 8 kWh → available = 10-6 = 4, значит 4 из community, 4 из grid
        EnergyMessage msg = new EnergyMessage("USER", "COMMUNITY", 8.0, hour.plusSeconds(300));

        // when
        service.onMessage(msg);

        // then: communityUsed = 6+4 = 10, gridUsed = 0+4 = 4
        ArgumentCaptor<HourlyUsage> capt = ArgumentCaptor.forClass(HourlyUsage.class);
        verify(repo).save(capt.capture());
        HourlyUsage updated = capt.getValue();

        assertEquals(10.0, updated.getCommunityProduced(), 1e-6);
        assertEquals(10.0, updated.getCommunityUsed(),     1e-6);
        assertEquals(4.0,  updated.getGridUsed(),          1e-6);

        // и отправка в очередь
        verify(rabbit).convertAndSend(updateQueue, updated);
    }
}
