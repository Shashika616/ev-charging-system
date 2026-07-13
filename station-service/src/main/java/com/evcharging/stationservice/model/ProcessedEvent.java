package com.evcharging.stationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    private String eventId; // The unique booking ID from the event

    @Column(nullable = false)
    private String eventType; // e.g., "BOOKING_CONFIRMED"

    @Column(nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();
}