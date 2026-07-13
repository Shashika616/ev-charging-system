package com.evcharging.bookingservice.controller;

import com.evcharging.bookingservice.model.Booking;
import com.evcharging.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestBody Booking booking,
            @AuthenticationPrincipal Jwt jwt) { // Extract the logged-in user's ID from Asgardeo!
        
        // Stamp the booking with the actual logged-in user's ID so they can't fake it
        booking.setUserId(jwt.getSubject()); 
        
        Booking createdBooking = bookingService.createBooking(booking);
        return ResponseEntity.ok(createdBooking);
    }
}