package com.gruppeM.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Random;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class ProducerApplication {

    @Value("${energy.association}")
    private String community;

    private final Random rnd = new Random();

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    @Scheduled(fixedRate = 5000)
    public void produce() {
        double kwh = 5 + rnd.nextDouble() * 5;
        log.info("PRODUCER, assoc={}, kwh={}, time={}",
                community, String.format("%.2f", kwh), Instant.now());
    }
}
