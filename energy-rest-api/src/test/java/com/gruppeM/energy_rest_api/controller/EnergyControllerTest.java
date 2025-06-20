package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.model.EnergyData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EnergyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private com.gruppeM.energy_rest_api.service.EnergyService energyService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        // передаем «energy.input» напрямую в конструктор
        var controller = new EnergyController(energyService, rabbitTemplate, "energy.input");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetCurrent() throws Exception {
        var now = LocalDateTime.of(2025,6,19,12,0);
        given(energyService.getCurrentEnergyStatus())
                .willReturn(new EnergyData(now, 100.0, 5.5));

        mockMvc.perform(get("/energy/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communityDepleted").value(100.0))
                .andExpect(jsonPath("$.gridPortion").value(5.5));
    }

    @Test
    void testGetHistorical() throws Exception {
        var start = LocalDateTime.of(2025,6,19,0,0);
        var end   = LocalDateTime.of(2025,6,19,3,0);
        var data = List.of(
                new EnergyData(start.plusHours(1), 95.0, 3.2),
                new EnergyData(start.plusHours(2), 97.5, 4.1)
        );
        given(energyService.getHistoricalEnergyData(start, end)).willReturn(data);

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
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        // убедимся, что rabbitTemplate.convertAndSend вызвано с нужными аргументами
        verify(rabbitTemplate).convertAndSend(
                eq("energy.input"),
                any(com.gruppeM.energy_rest_api.model.EnergyData.class)
        );
    }
}
