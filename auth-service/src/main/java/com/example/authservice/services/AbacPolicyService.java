package com.example.authservice.services;

import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public interface AbacPolicyService {
    void updatePolicy(String service, String function, Map<String, Object> conditions);

    void updateUserAttributes(String userId, Map<String, Object> attributes);

    Map<String, Object> getPolicy(String service, String function);

    Map<String, Object> getUserAttributes(String userId);

    boolean evaluatePolicy(String service, String function, String userId, Map<String, Object> requestAttrs);


}
