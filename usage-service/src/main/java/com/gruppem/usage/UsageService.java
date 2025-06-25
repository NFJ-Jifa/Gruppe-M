package com.gruppem.usage;

import com.gruppem.usage.EnergyMessage;
import com.gruppem.usage.HourlyUsage;
import com.gruppem.usage.HourlyUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UsageService {
    private static final Logger log = LoggerFactory.getLogger(UsageService.class);

    private final HourlyUsageRepository repo;
    private final RabbitTemplate rabbit;
    private final String updateQueue;

    public UsageService(HourlyUsageRepository repo,
                        RabbitTemplate rabbit,
                        @Value("${energy.update-queue}") String updateQueue) {
        this.repo = repo;
        this.rabbit = rabbit;
        this.updateQueue = updateQueue;
    }

    @RabbitListener(queues = "${energy.input-queue}")
    @Transactional
    public void onMessage(EnergyMessage msg) {
        Instant hourKey = msg.getDatetime().truncatedTo(ChronoUnit.HOURS);
        HourlyUsage usage = repo.findById(hourKey)
                .orElse(new HourlyUsage(hourKey));

        if ("PRODUCER".equals(msg.getType())) {
            // Производство всегда от сообщества
            usage.setCommunityProduced(usage.getCommunityProduced() + msg.getKwh());

        } else if ("USER".equals(msg.getType())) {
            switch (msg.getAssociation()) {
                case "COMMUNITY" -> {
                    double available = usage.getCommunityProduced() - usage.getCommunityUsed();
                    if (msg.getKwh() <= available) {
                        // всё в сообщество
                        usage.setCommunityUsed(usage.getCommunityUsed() + msg.getKwh());
                    } else {
                        // часть в сообщество
                        usage.setCommunityUsed(usage.getCommunityUsed() + available);
                        // остаток в сеть
                        double gridDelta = msg.getKwh() - available;
                        usage.setGridUsed(usage.getGridUsed() + gridDelta);
                        log.info("Hour {}: split USER-COMMUNITY {} into community={}, grid={}",
                                hourKey, msg.getKwh(), available, gridDelta);
                    }
                }
                case "GRID" -> {
                    // всё в сеть
                    usage.setGridUsed(usage.getGridUsed() + msg.getKwh());
                }
                default -> {
                    // fallback-сплит для некорректных association
                    double available = usage.getCommunityProduced() - usage.getCommunityUsed();
                    double communityPart = Math.min(available, msg.getKwh());
                    double gridPart = msg.getKwh() - communityPart;
                    usage.setCommunityUsed(usage.getCommunityUsed() + communityPart);
                    usage.setGridUsed(usage.getGridUsed() + gridPart);
                    log.warn("Unknown association '{}', split {} into community={}, grid={}",
                            msg.getAssociation(), msg.getKwh(), communityPart, gridPart);
                }
            }

        } else {
            log.warn("Unknown message type '{}'", msg.getType());
        }

        repo.save(usage);
        rabbit.convertAndSend(updateQueue, usage);
        log.debug("Hour {}: produced={} used={} grid={}",
                hourKey,
                usage.getCommunityProduced(),
                usage.getCommunityUsed(),
                usage.getGridUsed());
    }


}