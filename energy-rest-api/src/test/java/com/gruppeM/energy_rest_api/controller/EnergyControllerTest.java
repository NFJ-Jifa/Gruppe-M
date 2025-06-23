package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.model.HourlyUsage;
import com.gruppeM.energy_rest_api.repository.HourlyUsageRepository;
import com.gruppeM.energy_rest_api.service.EnergyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EnergyControllerTest {

    private MockMvc mockMvc;

    // моки для зависимостей
    private EnergyService energyService;
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private HourlyUsageRepository hourlyRepo;

    // имя очереди
    private final String inputQueue = "energy.input";

    @BeforeEach
    void setUp() {
        energyService  = mock(EnergyService.class);
        rabbitTemplate = mock(org.springframework.amqp.rabbit.core.RabbitTemplate.class);
        hourlyRepo     = mock(HourlyUsageRepository.class);

        // создаём контроллер вручную, прокидывая зависимости
        var controller = new com.gruppeM.energy_rest_api.controller.EnergyController(
                energyService,
                rabbitTemplate,
                hourlyRepo,
                inputQueue
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getCurrent_ShouldReturnJson() throws Exception {
        Instant now = Instant.parse("2025-06-19T12:00:00Z");
        given(energyService.getCurrentEnergyStatus())
                .willReturn(new EnergyData(now, 100.0, 5.5));

        mockMvc.perform(get("/energy/current"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.communityDepleted").value(100.0))
                .andExpect(jsonPath("$.gridPortion").value(5.5));
    }

    @Test
    void getHistorical_ShouldReturnListOrNoContent() throws Exception {
        Instant start = Instant.parse("2025-06-19T00:00:00Z");
        Instant end   = Instant.parse("2025-06-19T03:00:00Z");

        // пустой список → 204
        given(energyService.getHistoricalEnergyData(start, end))
                .willReturn(List.of());

        mockMvc.perform(get("/energy/historical")
                        .param("start", start.toString())
                        .param("end",   end.toString()))
                .andExpect(status().isNoContent());

        // непустой → 200 + body
        Instant a = Instant.parse("2025-06-19T01:00:00Z");
        Instant b = Instant.parse("2025-06-19T02:00:00Z");
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
    void publish_ShouldSendToRabbit_andReturnOk() throws Exception {
        String payload = """
            {
              "hour":"2025-06-19T15:00:00Z",
              "communityDepleted":80.0,
              "gridPortion":10.0
            }
            """;

        mockMvc.perform(post("/energy/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // проверяем, что отправка в Rabbit прошла
        ArgumentCaptor<EnergyData> cap = ArgumentCaptor.forClass(EnergyData.class);
        verify(rabbitTemplate).convertAndSend(eq(inputQueue), cap.capture());

        EnergyData sent = cap.getValue();
        assertThat(sent.getHour())
                .isEqualTo(Instant.parse("2025-06-19T15:00:00Z"));
        assertThat(sent.getCommunityDepleted()).isEqualTo(80.0);
        assertThat(sent.getGridPortion()).isEqualTo(10.0);
    }

    @Test
    void availableRange_ShouldReturnMinMaxFromRepo() throws Exception {
        // подготавливаем несколько HourlyUsage
        var h1 = new HourlyUsage(Instant.parse("2025-06-19T01:00:00Z"));
        var h2 = new HourlyUsage(Instant.parse("2025-06-19T05:00:00Z"));
        given(hourlyRepo.findAll()).willReturn(List.of(h1, h2));

        mockMvc.perform(get("/energy/available-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2025-06-19T01:00:00Z"))
                .andExpect(jsonPath("$.to"  ).value("2025-06-19T05:00:00Z"));
    }
}
