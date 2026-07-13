package com.evcharging.bookingservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Allows us to use @PreAuthorize in controllers
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Stateless API, no CSRF needed
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Allow Render's health checks to pass without a token
                .requestMatchers("/actuator/health", "/v3/api-docs/**").permitAll() 
                // Anyone with a valid Asgardeo token can GET chargers
                .requestMatchers(HttpMethod.GET, "/api/chargers/**").hasRole("DRIVER") 
                // Only Station Owners can add/modify chargers
                .requestMatchers(HttpMethod.POST, "/api/chargers/**").hasRole("STATION_OWNER")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    // Maps Asgardeo's 'groups' or 'roles' claim to Spring Security's ROLE_ prefix
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("sub");
        // Asgardeo puts roles in the 'groups' claim by default. 
        // We extract them and prefix with "ROLE_" so Spring's hasRole() works.
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var groups = jwt.getClaimAsStringList("groups");
            if (groups == null) return java.util.List.of();
            return groups.stream()
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    .collect(java.util.stream.Collectors.toList());
        });
        return converter;
    }
}