package com.example.gatewayservice.configs;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitingFilter  {
    /**
     * 3️⃣ Rate Limiter (global config)
     */
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 200); // default-filters: 100 req/s, burst 200
    }
    /**
     * 4️⃣ KeyResolver (giới hạn theo IP)
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (ip == null) {
                ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            }
            return Mono.just(ip);
        };
    }
}
