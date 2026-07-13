package com.evcharging.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/actuator/health")
    public Mono<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "api-gateway");
        health.put("timestamp", LocalDateTime.now().toString());
        
        Map<String, String> services = new HashMap<>();
        services.put("station-service", "http://localhost:8081");
        services.put("booking-service", "http://localhost:8082");
        services.put("grid-service", "http://localhost:8083");
        health.put("downstream-services", services);
        
        return Mono.just(health);
    }
}