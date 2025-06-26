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
        // Initialize service with mocked repository and rabbit template
        service = new UsageService(repo, rabbit, updateQueue);
    }

    @Test
    void onMessage_producerIncrementsCommunityProduced() {
        // Given: No existing record for the specified hour
        Instant ts = Instant.parse("2025-06-22T10:15:30Z").truncatedTo(ChronoUnit.HOURS);
        when(repo.findById(ts)).thenReturn(Optional.empty());

        EnergyMessage msg = new EnergyMessage("PRODUCER", "COMMUNITY", 5.5, ts.plusSeconds(900));

        // When: Message is processed
        service.onMessage(msg);

        // Then: Record should be saved with communityProduced = 5.5 and the rest = 0
        ArgumentCaptor<HourlyUsage> capt = ArgumentCaptor.forClass(HourlyUsage.class);
        verify(repo).save(capt.capture());
        HourlyUsage saved = capt.getValue();

        assertEquals(ts,    saved.getHourKey());
        assertEquals(5.5,   saved.getCommunityProduced(), 1e-6);
        assertEquals(0.0,   saved.getCommunityUsed(),     1e-6);
        assertEquals(0.0,   saved.getGridUsed(),          1e-6);

        // And the message should be sent to the update queue
        verify(rabbit).convertAndSend(updateQueue, saved);
    }

    @Test
    void onMessage_userExceedsAvailableSplitsBetweenCommunityAndGrid() {
        // Given: Existing record with produced=10, used=6, grid=0
        Instant hour = Instant.parse("2025-06-22T11:00:00Z");
        HourlyUsage existing = new HourlyUsage(hour);
        existing.setCommunityProduced(10.0);
        existing.setCommunityUsed(6.0);
        existing.setGridUsed(0.0);
        when(repo.findById(hour)).thenReturn(Optional.of(existing));

        // User requests 8 kWh → 4 from community (10 - 6), 4 from grid
        EnergyMessage msg = new EnergyMessage("USER", "COMMUNITY", 8.0, hour.plusSeconds(300));

        // When: Message is processed
        service.onMessage(msg);

        // Then: communityUsed = 10, gridUsed = 4
        ArgumentCaptor<HourlyUsage> capt = ArgumentCaptor.forClass(HourlyUsage.class);
        verify(repo).save(capt.capture());
        HourlyUsage updated = capt.getValue();

        assertEquals(10.0, updated.getCommunityProduced(), 1e-6);
        assertEquals(10.0, updated.getCommunityUsed(),     1e-6);
        assertEquals(4.0,  updated.getGridUsed(),          1e-6);

        // And message should be sent to the queue
        verify(rabbit).convertAndSend(updateQueue, updated);
    }
}
