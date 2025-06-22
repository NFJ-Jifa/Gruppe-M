package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import com.gruppeM.energy_rest_api.service.EnergyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnergyController.class)
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnergyService energyService;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    // Мок для репозитория нужен, чтобы контекст собирался
    @MockBean
    private HourlyUsageRepository hourlyRepo;

    @Value("${energy.input-queue:energy.input}")
    private String inputQueue;

    @Test
    void testGetCurrent() throws Exception {
        // DataService возвращает EnergyData с LocalDateTime внутри
        LocalDateTime nowLdt = LocalDateTime.of(2025, 6, 19, 12, 0);
        given(energyService.getCurrentEnergyStatus())
                .willReturn(new EnergyData(nowLdt, 100.0, 5.5));

        mockMvc.perform(get("/energy/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityDepleted").value(100.0))
                .andExpect(jsonPath("$.gridPortion").value(5.5));
    }

    @Test
    void testGetHistorical() throws Exception {
        // Параметры эндпойнта теперь Instant (с Z)
        Instant start = Instant.parse("2025-06-19T00:00:00Z");
        Instant end   = Instant.parse("2025-06-19T03:00:00Z");

        // Сервис оперирует LocalDateTime, поэтому на входе он конвертирует:
        LocalDateTime a = LocalDateTime.ofInstant(start, ZoneOffset.UTC).plusHours(1);
        LocalDateTime b = LocalDateTime.ofInstant(start, ZoneOffset.UTC).plusHours(2);
        var data = List.of(
                new EnergyData(a, 95.0, 3.2),
                new EnergyData(b, 97.5, 4.1)
        );
        given(energyService.getHistoricalEnergyData(start, end))
                .willReturn(data);

        mockMvc.perform(get("/energy/historical")
                        .param("start", start.toString())
                        .param("end",   end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].communityDepleted").value(95.0))
                .andExpect(jsonPath("$[1].gridPortion").value(4.1));
    }

    @Test
    void testPublish() throws Exception {
        String body = """
            {
              "hour":"2025-06-19T15:00:00",
              "communityDepleted":80.0,
              "gridPortion":10.0
            }
            """;

        mockMvc.perform(post("/energy/publish")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk());

        // Проверяем, что в RabbitTemplate передалась наша очередь и объект EnergyData
        verify(rabbitTemplate).convertAndSend(
                eq(inputQueue),
                any(EnergyData.class)
        );
    }
}
