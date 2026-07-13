package com.evcharging.stationservice.service;

import com.evcharging.stationservice.model.Charger;
import com.evcharging.stationservice.repository.ChargerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChargerService {

    private final ChargerRepository chargerRepository;

    // When this is called, Spring checks Upstash Redis first. 
    // If the data is there, it returns it instantly (Cache Hit).
    // If not, it runs the method, fetches from Supabase, saves to Redis, and returns it (Cache Miss).
    @Cacheable(value = "allChargers", key = "'list'")
    public List<Charger> getAllChargers() {
        System.out.println(">>> FETCHING FROM SUPABASE DATABASE (SLOW) <<<");
        return chargerRepository.findAll();
    }

    // When we add a new charger, we must clear the cache so the next GET request 
    // fetches the fresh list from the database.
    @CacheEvict(value = "allChargers", key = "'list'")
    public Charger addCharger(Charger charger) {
        return chargerRepository.save(charger);
    }
    
    public Optional<Charger> getChargerById(Long id) {
        return chargerRepository.findById(id);
    }

    @CacheEvict(value = "allChargers", key = "'list'")
    public Charger updateCharger(Charger charger) {
        return chargerRepository.save(charger);
    }
}