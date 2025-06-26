package com.gruppeM.energy_rest_api.controller;

import com.gruppeM.energy_rest_api.dto.EnergyMessage;
import com.gruppeM.energy_rest_api.dto.HistoricalUsageDto;
import com.gruppeM.energy_rest_api.model.EnergyData;
import com.gruppeM.energy_rest_api.model.AvailableRange;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the EnergyController.
 * These tests verify that the controller returns the correct HTTP responses
 * and interacts correctly with its dependencies.
 */
class EnergyControllerTest {

    private MockMvc mockMvc; // Mocks the HTTP layer
    private HourlyUsageRepository hourlyRepo;
    private EnergyService energyService;
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    private final String inputQueue = "energy.input";

    /**
     * Sets up a mocked controller with all dependencies.
     * This runs before each test method.
     */
    @BeforeEach
    void setUp() {
        hourlyRepo     = mock(HourlyUsageRepository.class);
        energyService  = mock(EnergyService.class);
        rabbitTemplate = mock(org.springframework.amqp.rabbit.core.RabbitTemplate.class);

        var controller = new EnergyController(hourlyRepo, energyService, rabbitTemplate, inputQueue);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Tests the GET /energy/current endpoint.
     * Expects a JSON response with communityDepleted and gridPortion.
     */
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

    /**
     * Tests the GET /energy/historical endpoint with different cases:
     * - If the returned list is empty → 204 No Content.
     * - If the list contains data → 200 OK with JSON array.
     */
    @Test
    void getHistorical_ShouldReturnListOrNoContent() throws Exception {
        Instant start = Instant.parse("2025-06-19T00:00:00Z");
        Instant end   = Instant.parse("2025-06-19T03:00:00Z");

        // Case 1: empty list → expect HTTP 204
        given(energyService.getHistoricalEnergyData(start, end))
                .willReturn(List.of());

        mockMvc.perform(get("/energy/historical")
                        .param("start", start.toString())
                        .param("end",   end.toString()))
                .andExpect(status().isNoContent());

        // Case 2: non-empty list → expect HTTP 200 with body
        Instant a = Instant.parse("2025-06-19T01:00:00Z");
        Instant b = Instant.parse("2025-06-19T02:00:00Z");

        var data = List.of(
                new HistoricalUsageDto(a, 10.0, 9.5, 0.5, 95.0, 5.0),
                new HistoricalUsageDto(b, 8.0,  7.72, 0.28, 96.5, 3.5)
        );
        given(energyService.getHistoricalEnergyData(start, end)).willReturn(data);

        mockMvc.perform(get("/energy/historical")
                        .param("start", start.toString())
                        .param("end",   end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].communityDepleted").value(95.0))
                .andExpect(jsonPath("$[1].gridPortion").value(3.5));
    }

    /**
     * Tests the POST /energy/publish endpoint.
     * Verifies that a valid EnergyMessage is sent to the input queue.
     */
    @Test
    void publishRaw_ShouldSendToRabbit_andReturnAccepted() throws Exception {
        String payload = """
            {
              "type": "USER",
              "association": "COMMUNITY",
              "kwh": 3.5,
              "datetime": "2025-06-19T15:00:00Z"
            }
            """;

        mockMvc.perform(post("/energy/publish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted());

        // Capture the message sent to RabbitMQ and verify its fields
        ArgumentCaptor<EnergyMessage> cap = ArgumentCaptor.forClass(EnergyMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(inputQueue), cap.capture());

        EnergyMessage sent = cap.getValue();
        assertThat(sent.getType()).isEqualTo("USER");
        assertThat(sent.getAssociation()).isEqualTo("COMMUNITY");
        assertThat(sent.getKwh()).isEqualTo(3.5);
        assertThat(sent.getDatetime()).isEqualTo(Instant.parse("2025-06-19T15:00:00Z"));
    }

    /**
     * Tests the GET /energy/available-range endpoint.
     * Simulates finding the earliest and latest available timestamps from the repository.
     */
    @Test
    void availableRange_ShouldReturnMinMaxFromRepo() throws Exception {
        var h1 = new com.gruppeM.energy_rest_api.model.HourlyUsage(Instant.parse("2025-06-19T01:00:00Z"));
        var h2 = new com.gruppeM.energy_rest_api.model.HourlyUsage(Instant.parse("2025-06-19T05:00:00Z"));

        given(hourlyRepo.findMinHourKey()).willReturn(Optional.of(h1.getHourKey()));
        given(hourlyRepo.findMaxHourKey()).willReturn(Optional.of(h2.getHourKey()));

        mockMvc.perform(get("/energy/available-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2025-06-19T01:00:00Z"))
                .andExpect(jsonPath("$.to").value("2025-06-19T05:00:00Z"));
    }
}
