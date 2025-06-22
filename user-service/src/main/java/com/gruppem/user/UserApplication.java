package com.gruppem.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Random;

@SpringBootApplication
@EnableScheduling
public class UserApplication {

    private static final Logger log = LoggerFactory.getLogger(UserApplication.class);

    @Value("${energy.association}")
    private String association;

    @Value("${energy.type}")
    private String type;

    private final RabbitTemplate rabbitTemplate;
    private final String queue;

    public UserApplication(RabbitTemplate rabbitTemplate,
                           @Value("${energy.queue}") String queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
    }

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        log.info("UserService started");
    }

    @Scheduled(fixedDelayString = "#{T(java.util.concurrent.ThreadLocalRandom).current().nextInt(1000,5000)}")
    public void produceUsage() {
        // Base consumption 0–2 kWh, peaks add another 1–3 kWh
        double base = new Random().nextDouble() * 2.0;
        LocalTime now = LocalTime.now();
        if ((now.isAfter(LocalTime.of(6, 59)) && now.isBefore(LocalTime.of(9, 1))) ||
                (now.isAfter(LocalTime.of(17, 59)) && now.isBefore(LocalTime.of(20, 1)))) {
            base += 1.0 + new Random().nextDouble() * 3.0;
        }
        EnergyMessage msg = new EnergyMessage(type, association, base, Instant.now());
        rabbitTemplate.convertAndSend(queue, msg);
        log.info("Sent to {}: {}", queue, msg);
    }
}
