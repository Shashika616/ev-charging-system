package com.evcharging.gridservice.service;

import com.evcharging.gridservice.dto.BookingEvent;
import com.evcharging.gridservice.model.GridZone;
import com.evcharging.gridservice.model.OutboxEvent;
import com.evcharging.gridservice.repository.GridZoneRepository;
import com.evcharging.gridservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GridCalculationService {

    private final GridZoneRepository gridZoneRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void processNewBookingLoad(BookingEvent event) {
        Long zoneId = event.getGridZoneId();
        Integer chargerMaxKw = event.getChargerMaxKw(); // Get the REAL charger power from the event
        
        // 1. Fetch the specific grid zone for this booking
        Optional<GridZone> zoneOpt = gridZoneRepository.findById(zoneId);
        
        if (zoneOpt.isEmpty()) {
            log.error("Grid zone {} not found! Cannot process booking.", zoneId);
            throw new RuntimeException("Grid zone not found: " + zoneId);
        }

        GridZone zone = zoneOpt.get();

        // 2. Update the grid zone's current load with the ACTUAL charger power
        int newLoad = zone.getCurrentLoadKw() + chargerMaxKw;
        zone.setCurrentLoadKw(newLoad);
        gridZoneRepository.save(zone);

        log.info("Updated Grid Zone {} load to {} kW (added {} kW charger, capacity: {} kW)", 
                zoneId, newLoad, chargerMaxKw, zone.getTransformerCapacityKw());

        // 3. CHECK CAPACITY: Is the load over 80%?
        double capacityThreshold = zone.getTransformerCapacityKw() * 0.80;
        
        if (newLoad > capacityThreshold) {
            log.warn(">>> WARNING: Grid Zone {} load exceeds 80%! Triggering Price Surge.", zoneId);
            
            // 4. Create an Outbox Event for the Price Surge
            OutboxEvent surgeEvent = new OutboxEvent();
            surgeEvent.setEventType("PRICE_SURGE");
            surgeEvent.setPayload(String.format(
                    "{\"zoneId\": %d, \"multiplier\": 1.5, \"currentLoad\": %d, \"capacity\": %d}",
                    zone.getId(), newLoad, zone.getTransformerCapacityKw()
            ));
            surgeEvent.setAggregateId(event.getBookingId());
            surgeEvent.setStatus(OutboxEvent.EventStatus.PENDING);
            
            outboxEventRepository.save(surgeEvent);
            log.info("Price surge event saved to outbox for zone {}", zoneId);
        }
    }
}