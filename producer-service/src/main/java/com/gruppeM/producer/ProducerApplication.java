package com.gruppeM.producer;

import com.gruppeM.producer.EnergyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
@EnableScheduling
public class ProducerApplication {

    private static final Logger log = LoggerFactory.getLogger(ProducerApplication.class);

    private final RabbitTemplate rabbit;
    private final String queue;
    private final String type;
    private final String association;

    public ProducerApplication(RabbitTemplate rabbit,
                               @Value("${energy.input-queue}") String queue,
                               @Value("${energy.type}") String type,
                               @Value("${energy.association}") String association) {
        this.rabbit = rabbit;
        this.queue = queue;
        this.type = type;
        this.association = association;
    }

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
        log.info("ProducerService started");
    }

    @Scheduled(fixedDelayString = "#{T(java.util.concurrent.ThreadLocalRandom).current().nextInt(1000,5000)}")
    public void produce() {
        // Reduced energy production to 0–2 kWh to allow for grid consumption when needed
        double kwh = ThreadLocalRandom.current().nextDouble() * 2.0;
        EnergyMessage msg = new EnergyMessage(type, association, kwh, Instant.now());
        rabbit.convertAndSend(queue, msg);
        log.info("Sent to {}: {}", queue, msg);
    }
}
