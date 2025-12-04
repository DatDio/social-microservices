package com.example.authservice.services.Implements;

import com.example.authservice.events.models.AbacPolicyChangedEvent;
import com.example.authservice.events.models.UserAttributesChangedEvent;
import com.example.authservice.services.AbacPolicyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class AbacPolicyServiceImpl implements AbacPolicyService {
    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Keycloak keycloak;
    private final ObjectMapper objectMapper;
    private static final String POLICY_PREFIX = "abac_policy:";
    private static final String USER_ATTRS_PREFIX = "user_attributes:";

    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.resource}")
    private String clientId;

    @Override
    public void updatePolicy(String service, String function, Map<String, Object> conditions) {
        try {
            // Lưu vào Keycloak Client Attributes
            ClientRepresentation client = keycloak.realm(realm).clients().findByClientId(clientId).get(0);
            String policyKey = "abac_policy:" + service + ":" + function;
            String json = objectMapper.writeValueAsString(conditions); // có thể ném JsonProcessingException
            client.getAttributes().put(policyKey, json);
            keycloak.realm(realm).clients().get(client.getId()).update(client);

            String key = POLICY_PREFIX + service + ":" + function;
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                connection.hMSet(key.getBytes(), serializeMap(conditions));
                connection.expire(key.getBytes(), 3600);
                return null;
            });

            kafkaTemplate.send("abac-policy-changed", new AbacPolicyChangedEvent(service, function, conditions));

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể lưu policy vào Keycloak", e);
        }
    }


    @Override
    public void updateUserAttributes(String userId, Map<String, Object> attributes) {
        String key = USER_ATTRS_PREFIX + userId;
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.hMSet(key.getBytes(), serializeMap(attributes));
            connection.expire(key.getBytes(), 3600);
            return null;
        });

        kafkaTemplate.send("user-attributes-changed", new UserAttributesChangedEvent(userId, attributes));
    }

    public Map<String, Object> getPolicy(String service, String function) {
        String key = "abac_policy:" + service + ":" + function;
        Map<String, Object> policy = redisTemplate.opsForHash().entries(key);
        if (policy.isEmpty()) {
            // Fallback từ Keycloak
            ClientRepresentation client = keycloak.realm(realm).clients().findByClientId(clientId).get(0);
            String policyJson = client.getAttributes().get(key);
            if (policyJson != null) {
                try {
                    policy = objectMapper.readValue(policyJson, Map.class);
                    redisTemplate.opsForHash().putAll(key, serializeMap(policy));
                    redisTemplate.expire(key, 3600, TimeUnit.SECONDS);
                } catch (Exception e) {
                    // Log error
                }
            }
        }
        return policy;
    }

    @Override
    public Map<String, Object> getUserAttributes(String userId) {
        String key = USER_ATTRS_PREFIX + userId;
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public boolean evaluatePolicy(String service, String function, String userId, Map<String, Object> requestAttrs) {
        Map<String, Object> policy = getPolicy(service, function);
        if (policy.isEmpty()) return false;

        Map<String, Object> userAttrs = getUserAttributes(userId);
        for (Map.Entry<String, Object> entry : policy.entrySet()) {
            String attr = entry.getKey();
            String condition = (String) entry.getValue();
            String userValue = (String) userAttrs.getOrDefault(attr, "");
            String requestValue = (String) requestAttrs.getOrDefault(attr, "");

            if (!matchesCondition(userValue, requestValue, condition)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesCondition(String userValue, String requestValue, String condition) {
        if (condition.contains("|")) {
            return Arrays.stream(condition.split("\\|")).anyMatch(val -> val.equals(userValue) || val.equals(requestValue));
        } else if (condition.startsWith(">")) {
            try {
                double val = Double.parseDouble(userValue);
                double condVal = Double.parseDouble(condition.substring(1));
                return val > condVal;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return condition.equals(userValue) || condition.equals(requestValue);
    }

    private Map<byte[], byte[]> serializeMap(Map<String, Object> map) {
        Map<byte[], byte[]> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k.getBytes(), v.toString().getBytes()));
        return result;
    }
}
