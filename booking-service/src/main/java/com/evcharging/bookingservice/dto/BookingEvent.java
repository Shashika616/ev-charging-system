package com.evcharging.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEvent {
    private Long chargerId;
    private Long gridZoneId;
    private Integer chargerMaxKw; 
    private String status;
    private String bookingId;
}