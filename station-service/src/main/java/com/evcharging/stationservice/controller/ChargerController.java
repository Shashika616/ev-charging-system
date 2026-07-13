package com.evcharging.stationservice.controller;

import com.evcharging.stationservice.model.Charger;
import com.evcharging.stationservice.service.ChargerService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chargers")
@RequiredArgsConstructor
public class ChargerController {
;
    private final ChargerService chargerService;


    @GetMapping
    public ResponseEntity<List<Charger>> getAllChargers() {
        // In the next step, we will add Redis caching here!
        return ResponseEntity.ok(chargerService.getAllChargers());
    }

    @PostMapping
    @PreAuthorize("hasRole('STATION_OWNER')")
    public ResponseEntity<Charger> addCharger(@RequestBody Charger charger) {
        return ResponseEntity.ok(chargerService.addCharger(charger));
    }
}