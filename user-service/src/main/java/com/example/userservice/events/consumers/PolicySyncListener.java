package com.example.userservice.events.consumers;

import com.example.userservice.events.models.UserAttributesChangedEvent;
import com.example.userservice.events.models.AbacPolicyChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PolicySyncListener {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @KafkaListener(topics = "abac-policy-changed", groupId = "user-service-group")
    public void handlePolicyChange(AbacPolicyChangedEvent event) {
        if ("user-service".equals(event.getService())) {
            String key = "abac_policy:" + event.getService() + ":" + event.getFunction();
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                connection.hMSet(key.getBytes(), serializeMap(event.getConditions()));
                connection.expire(key.getBytes(), 3600);
                return null;
            });
        }
    }

    @KafkaListener(topics = "user-attributes-changed", groupId = "user-service-group")
    public void handleUserAttributesChange(UserAttributesChangedEvent event) {
        String key = "user_attributes:" + event.getUserId();
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.hMSet(key.getBytes(), serializeMap(event.getAttributes()));
            connection.expire(key.getBytes(), 3600);
            return null;
        });
    }

    private Map<byte[], byte[]> serializeMap(Map<String, Object> map) {
        Map<byte[], byte[]> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k.getBytes(), v.toString().getBytes()));
        return result;
    }
}
