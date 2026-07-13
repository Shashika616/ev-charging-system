package com.evcharging.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity  // Note: WebFlux, NOT WebSecurity (because Gateway is reactive)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())  // Stateless API, no CSRF needed
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints (no auth required)
                .pathMatchers("/actuator/health").permitAll()
                .pathMatchers("/api/stations/api/chargers").permitAll()  // Allow browsing chargers
                
                // Protected endpoints (require authentication)
                .pathMatchers("/api/bookings/**").authenticated()
                .pathMatchers("/api/grid/**").authenticated()
                
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})  // Use default JWT validation
            );

        return http.build();
    }
}