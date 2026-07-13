package com.evcharging.gridservice.scheduler;

import com.evcharging.gridservice.model.OutboxEvent;
import com.evcharging.gridservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus(OutboxEvent.EventStatus.PENDING);

        if (pendingEvents.isEmpty()) return;

        log.info("Grid Service: Found {} pending events. Publishing...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // We use a different routing key for price surges!
                String routingKey = "grid.price.surge"; 
                rabbitTemplate.convertAndSend("ev-charging-exchange", routingKey, event.getPayload());
                
                event.setStatus(OutboxEvent.EventStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                
            } catch (Exception e) {
                log.error("Failed to publish grid event", e);
                event.setStatus(OutboxEvent.EventStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}