package com.example.gateway_service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebFilter;

@Configuration
public class GatewaySecurityConfig {

    private final JwtWebFilter jwtWebFilter;

    public GatewaySecurityConfig(JwtWebFilter jwtWebFilter) {
        this.jwtWebFilter = jwtWebFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain() {
        return ServerHttpSecurity.http()
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers("/hobbies/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                )
                .addFilterAt((WebFilter) jwtWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
