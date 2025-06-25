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
import java.util.function.Supplier;

@SpringBootApplication
@EnableScheduling
public class UserApplication {

    private static final Logger log = LoggerFactory.getLogger(UserApplication.class);

    // Инжектим из application.yml
    @Value("${energy.type}")
    private String type;

    @Value("${energy.association}")
    private String association;

    private final RabbitTemplate rabbitTemplate;
    private final String queue;

    // Поставщики «текущего времени» — по умолчанию реальные, в тестах подменяем
    private Supplier<Instant> nowSupplier  = Instant::now;
    private Supplier<LocalTime> timeSupplier = LocalTime::now;

    public UserApplication(RabbitTemplate rabbitTemplate,
                           @Value("${energy.queue}") String queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
    }

    // Сеттеры для тестов
    public void setType(String type) {
        this.type = type;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public void setNowSupplier(Supplier<Instant> nowSupplier) {
        this.nowSupplier = nowSupplier;
    }

    public void setTimeSupplier(Supplier<LocalTime> timeSupplier) {
        this.timeSupplier = timeSupplier;
    }

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        log.info("UserService started");
    }

    @Scheduled(fixedDelayString =
            "#{T(java.util.concurrent.ThreadLocalRandom).current().nextInt(1000,5000)}")
    public void produceUsage() {
        Instant timestamp = nowSupplier.get();
        LocalTime currentTime = timeSupplier.get();

        double base = new Random().nextDouble() * 2.0;
        if ((currentTime.isAfter(LocalTime.of(6, 59)) && currentTime.isBefore(LocalTime.of(9, 1))) ||
                (currentTime.isAfter(LocalTime.of(17,59)) && currentTime.isBefore(LocalTime.of(20,1)))) {
            base += 1.0 + new Random().nextDouble() * 3.0;
        }

        double communityKwh = Math.min(base, 2.0);
        double gridKwh = base - communityKwh;

        // всегда шлём сообщение COMMUNITY (если >0)
        if (communityKwh > 0) {
            EnergyMessage communityMsg = new EnergyMessage(
                    type, association, communityKwh, timestamp
            );
            rabbitTemplate.convertAndSend(queue, communityMsg);
            log.info("Sent COMMUNITY usage: {}", communityMsg);
        }

        // теперь — всегда второе GRID (может быть 0.0)
        EnergyMessage gridMsg = new EnergyMessage(
                type, "GRID", gridKwh, timestamp
        );
        rabbitTemplate.convertAndSend(queue, gridMsg);
        log.info("Sent GRID usage: {}", gridMsg);
    }

}
