package com.example.authservice.services;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface KeycloakService {
    /*Đồng bộ user từ keyCloak vào redis */
    void syncUsersFromKeycloak();

    void syncUserFromKeycloak(String userId);

    void updateUserAttributesInKeycloak(String userId, Map<String, Object> attributes);
}