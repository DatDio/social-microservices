package com.example.gatewayservice.configs;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfigs {
    /**
     * Định nghĩa Route bằng code thay cho YAML
     * Có thể thêm nhiều service dễ dàng
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           RedisRateLimiter redisRateLimiter,
                                           KeyResolver ipKeyResolver) {
        return builder.routes()
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.stripPrefix(2)
                                .requestRateLimiter(c -> {
                                    c.setRateLimiter(redisRateLimiter);
                                    c.setKeyResolver(ipKeyResolver);
                                }))
                        .uri("lb://user-service"))
                // 🔥 Bạn có thể thêm nhiều route mới ở đây
                // .route("order-service", r -> r.path("/api/orders/**") ...)
                .build();
    }

}
