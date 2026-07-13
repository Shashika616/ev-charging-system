package com.evcharging.bookingservice.scheduler;

import com.evcharging.bookingservice.model.OutboxEvent;
import com.evcharging.bookingservice.repository.OutboxEventRepository;
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

    // This method runs every 5 seconds (5000 milliseconds)
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void publishPendingEvents() {
        // 1. Fetch all pending events from the outbox table
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findByStatus(OutboxEvent.EventStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return; // Nothing to do
        }

        log.info("Found {} pending events in outbox. Publishing to RabbitMQ...", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // 2. Publish to RabbitMQ
                String routingKey = "booking.confirmed";
                rabbitTemplate.convertAndSend("ev-charging-exchange", routingKey, event.getPayload());
                
                // 3. Mark as published
                event.setStatus(OutboxEvent.EventStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                
                log.info("Successfully published event ID: {} for booking ID: {}", 
                        event.getId(), event.getAggregateId());
                        
            } catch (Exception e) {
                // 4. If publishing fails, mark as FAILED so we can retry later
                log.error("Failed to publish event ID: {}. Marking as FAILED.", event.getId(), e);
                event.setStatus(OutboxEvent.EventStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}