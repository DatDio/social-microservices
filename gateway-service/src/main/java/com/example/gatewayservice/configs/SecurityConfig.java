package com.example.gatewayservice.configs;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * 1️⃣ Spring Security cấu hình OAuth2 + JWT
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/", "/public/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    @Bean
    public GlobalFilter logAndUserIdFilter() {
        return (exchange, chain) -> exchange.getPrincipal()
                .cast(org.springframework.security.oauth2.jwt.Jwt.class)
                .defaultIfEmpty(null)
                .flatMap(jwt -> {
                    String userId = jwt != null ? jwt.getSubject() : null;
                    String correlationId = UUID.randomUUID().toString();

                    var mutatedRequest = exchange.getRequest().mutate()
                            .header("X-Correlation-Id", correlationId)
                            .headers(headers -> {
                                if (userId != null) headers.set("X-User-Id", userId);
                            })
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }




}
