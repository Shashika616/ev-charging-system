package com.evcharging.bookingservice.service;

import com.evcharging.bookingservice.dto.BookingEvent;
import com.evcharging.bookingservice.model.Booking;
import com.evcharging.bookingservice.model.OutboxEvent;
import com.evcharging.bookingservice.repository.BookingRepository;
import com.evcharging.bookingservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Booking createBooking(Booking booking) {
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking saved to database with ID: {}", savedBooking.getId());

        // Create the event payload WITH chargerMaxKw
        BookingEvent event = new BookingEvent(
                savedBooking.getChargerId(),
                savedBooking.getGridZoneId(),
                savedBooking.getChargerMaxKw(), // NEW: Include the charger's power
                "RESERVED",
                savedBooking.getId().toString()
        );

        String eventJson;
        try {
            eventJson = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event to JSON", e);
            throw new RuntimeException("Failed to create booking event", e);
        }

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setEventType("BOOKING_CONFIRMED");
        outboxEvent.setPayload(eventJson);
        outboxEvent.setAggregateId(savedBooking.getId().toString());
        outboxEvent.setStatus(OutboxEvent.EventStatus.PENDING);
        
        outboxEventRepository.save(outboxEvent);
        log.info("Event saved to outbox table with chargerMaxKw: {} kW", savedBooking.getChargerMaxKw());

        return savedBooking;
    }
}