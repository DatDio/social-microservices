package com.example.gatewayservice.configs;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
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

    /**
     * 5️⃣ Global Filter: Log + gắn Correlation ID
     */
    @Bean
    public GlobalFilter logFilter() {
        return (exchange, chain) -> {
            String correlationId = UUID.randomUUID().toString();
            exchange.getRequest().mutate()
                    .header("X-Correlation-Id", correlationId)
                    .build();

            System.out.println("[Gateway] [" + LocalDateTime.now() + "] "
                    + exchange.getRequest().getMethod() + " "
                    + exchange.getRequest().getURI());

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        HttpStatus statusCode = (HttpStatus) exchange.getResponse().getStatusCode();
                        System.out.println("[Gateway] Response Status: " + statusCode);
                    }));
        };
    }


}
