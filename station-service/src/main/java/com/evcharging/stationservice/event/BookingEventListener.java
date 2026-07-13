package com.evcharging.stationservice.event;

import com.evcharging.stationservice.config.RabbitMQConfig;
import com.evcharging.stationservice.dto.BookingEvent;
import com.evcharging.stationservice.model.Charger;
import com.evcharging.stationservice.model.ProcessedEvent;
import com.evcharging.stationservice.repository.ProcessedEventRepository;
import com.evcharging.stationservice.service.ChargerService;
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

    private final ChargerService chargerService;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleBookingConfirmed(Message message, Channel channel) throws Exception {
        // Extract the delivery tag (unique ID for this specific delivery attempt)
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        
        try {
            // 1. Convert the message body from JSON string to BookingEvent object
            String jsonPayload = new String(message.getBody());
            BookingEvent event = objectMapper.readValue(jsonPayload, BookingEvent.class);
            
            log.info(">>> RECEIVED EVENT: Charger {} is now {}", 
                    event.getChargerId(), event.getStatus());

            // 2. IDEMPOTENCY CHECK: Have we already processed this event?
            String eventId = event.getBookingId();
            if (processedEventRepository.existsByEventId(eventId)) {
                log.warn(">>> DUPLICATE EVENT DETECTED: Booking ID {} already processed. ACKing and ignoring.", 
                        eventId);
                // Acknowledge the message so RabbitMQ removes it from the queue
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 3. Process the event
            chargerService.getChargerById(event.getChargerId()).ifPresent(charger -> {
                if ("RESERVED".equals(event.getStatus())) {
                    charger.setStatus(Charger.ChargerStatus.RESERVED);
                } else if ("IN_USE".equals(event.getStatus())) {
                    charger.setStatus(Charger.ChargerStatus.IN_USE);
                }
                
                // Save to Supabase & clear Redis cache
                chargerService.updateCharger(charger);
                log.info(">>> CHARGER STATUS UPDATED IN DB & CACHE CLEARED.");
            });

            // 4. Mark this event as processed (for idempotency)
            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(eventId);
            processedEvent.setEventType("BOOKING_CONFIRMED");
            processedEventRepository.save(processedEvent);

            // 5. MANUALLY ACKNOWLEDGE the message
            // This tells RabbitMQ: "I successfully processed this message, you can delete it."
            channel.basicAck(deliveryTag, false);
            log.info(">>> EVENT SUCCESSFULLY PROCESSED AND ACKNOWLEDGED.");

        } catch (DataIntegrityViolationException e) {
            // This happens if there's a race condition and two threads try to 
            // process the same event at the exact same time
            log.warn(">>> RACE CONDITION: Event already processed by another thread. ACKing.");
            channel.basicAck(deliveryTag, false);
            
        } catch (Exception e) {
            // Processing failed
            log.error(">>> ERROR PROCESSING EVENT: {}", e.getMessage(), e);
            
            // MANUALLY NACK (Negative Acknowledge) the message
            // Parameters: deliveryTag, multiple (false = just this message), requeue (false = send to DLQ)
            channel.basicNack(deliveryTag, false, false);
            log.error(">>> EVENT NACKED AND SENT TO DEAD LETTER QUEUE.");
        }
    }
}