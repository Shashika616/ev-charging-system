package com.evcharging.stationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private Long chargerId;
    private String status; // e.g., "RESERVED" or "IN_USE"
    private String bookingId;
}