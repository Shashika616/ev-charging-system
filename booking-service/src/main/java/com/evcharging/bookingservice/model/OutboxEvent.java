package com.evcharging.bookingservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType; // e.g., "BOOKING_CONFIRMED"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON string of the event data

    @Column(nullable = false)
    private String aggregateId; // The booking ID this event relates to

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.PENDING;

    @Column
    private LocalDateTime publishedAt;

    public enum EventStatus {
        PENDING, PUBLISHED, FAILED
    }
}