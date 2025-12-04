package com.example.userservice.configs;

import com.example.userservice.Dtos.AbacRequest;
import com.example.userservice.entities.User;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component("appAuthorizer")
public class AppAuthorizer {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public boolean authorize(Authentication authentication, String function, Object context) {
        String userId = authentication.getName();
        String service = "user-service";
        Map<String, Object> requestAttrs = extractRequestAttributes(context);

        String policyKey = "abac_policy:" + service + ":" + function;
        Map<String, Object> policy = redisTemplate.opsForHash().entries(policyKey);
        if (policy.isEmpty()) {
            return callAuthService(service, function, userId, requestAttrs);
        }

        String userKey = "user_attributes:" + userId;
        Map<String, Object> userAttrs = redisTemplate.opsForHash().entries(userKey);
        if (userAttrs.isEmpty()) {
            userAttrs = fetchUserAttributesFromKeycloak(userId);
        }

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

    private Map<String, Object> extractRequestAttributes(Object context) {
        Map<String, Object> attrs = new HashMap<>();
        if (context instanceof User) {
            User user = (User) context;
            attrs.put("resource_owner", user.getId());
            attrs.put("user_status", user.getStatus());
        }
        return attrs;
    }

    private boolean callAuthService(String service, String function, String userId, Map<String, Object> requestAttrs) {
        AbacRequest req = new AbacRequest();
        req.setService(service);
        req.setFunction(function);
        req.setUserId(userId);
        req.setRequestAttributes(requestAttrs);

        ResponseEntity<Boolean> response = restTemplate.postForEntity(
                "http://auth-service:8081/auth/abac/evaluate", req, Boolean.class
        );
        return response.getBody() != null && response.getBody();
    }

    private Map<String, Object> fetchUserAttributesFromKeycloak(String userId) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        Map<String, List<String>> attributes = user.getAttributes() != null ? user.getAttributes() : new HashMap<>();
        List<String> roles = keycloak.realm(realm).users().get(userId).roles().realmLevel().listEffective()
                .stream().map(RoleRepresentation::getName).collect(Collectors.toList());

        Map<String, Object> redisAttrs = new HashMap<>();
        attributes.forEach((k, v) -> redisAttrs.put(k, v.get(0)));
        redisAttrs.put("role", String.join("|", roles));

        String key = "user_attributes:" + userId;
        redisTemplate.opsForHash().putAll(key, redisAttrs);
        redisTemplate.expire(key, 3600, TimeUnit.SECONDS);
        return redisAttrs;
    }
}
