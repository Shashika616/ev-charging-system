package com.evcharging.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/station")
    public Mono<Map<String, Object>> stationFallback() {
        return Mono.just(createFallbackResponse("Station Service"));
    }

    @RequestMapping("/booking")
    public Mono<Map<String, Object>> bookingFallback() {
        return Mono.just(createFallbackResponse("Booking Service"));
    }

    @RequestMapping("/grid")
    public Mono<Map<String, Object>> gridFallback() {
        return Mono.just(createFallbackResponse("Grid Service"));
    }

    private Map<String, Object> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is currently down or experiencing high latency. Please try again shortly.");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("circuitBreaker", "OPEN");
        return response;
    }
}