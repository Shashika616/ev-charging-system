package com.evcharging.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        // Extract user ID from JWT token for rate limiting
        return exchange -> ReactiveSecurityContextHolder.getContext()
            .map(context -> {
                Authentication auth = context.getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                    return jwt.getSubject();  // Use user ID as the key
                }
                return "anonymous";  // Fallback for unauthenticated requests
            })
            .defaultIfEmpty("anonymous");
    }
}