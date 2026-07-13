package com.evcharging.gridservice.event;

import com.evcharging.gridservice.config.RabbitMQConfig;
import com.evcharging.gridservice.dto.BookingEvent;
import com.evcharging.gridservice.model.ProcessedEvent;
import com.evcharging.gridservice.repository.ProcessedEventRepository;
import com.evcharging.gridservice.service.GridCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final GridCalculationService gridCalculationService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleBookingConfirmed(Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            String jsonPayload = new String(message.getBody());
            BookingEvent event = objectMapper.readValue(jsonPayload, BookingEvent.class);
            
            log.info(">>> GRID SERVICE RECEIVED EVENT: Booking ID {}", event.getBookingId());

            // 1. IDEMPOTENCY CHECK
            String eventId = event.getBookingId();
            if (processedEventRepository.existsByEventId(eventId)) {
                log.warn(">>> DUPLICATE EVENT. ACKing and ignoring.");
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. PROCESS THE EVENT (Calculate Grid Load)
            gridCalculationService.processNewBookingLoad(event);

            // 3. MARK AS PROCESSED
            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(eventId);
            processedEvent.setEventType("BOOKING_CONFIRMED");
            processedEventRepository.save(processedEvent);

            // 4. MANUAL ACK
            channel.basicAck(deliveryTag, false);
            log.info(">>> GRID EVENT SUCCESSFULLY PROCESSED AND ACKNOWLEDGED.");

        } catch (DataIntegrityViolationException e) {
            log.warn(">>> RACE CONDITION: Event already processed. ACKing.");
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error(">>> ERROR PROCESSING GRID EVENT: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false); // Send to DLQ
        }
    }
}