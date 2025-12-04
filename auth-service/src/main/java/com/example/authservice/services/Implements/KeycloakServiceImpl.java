package com.example.authservice.services.Implements;

import com.example.authservice.services.AbacPolicyService;
import com.example.authservice.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService{
    private final AbacPolicyService abacPolicyService;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

//    public void startEventListener() {
//        keycloak.realm(realm).addEventListener(event -> {
//            if (event.getType() == org.keycloak.events.EventType.UPDATE_PROFILE) {
//                syncUserFromKeycloak(event.getUserId());
//            }
//        });
//    }
    @Scheduled(fixedRate = 60000) // Đồng bộ mỗi 60s
    public void syncUsersFromKeycloak() {
        List<org.keycloak.representations.idm.UserRepresentation> kcUsers = keycloak.realm(realm).users().list();
        kcUsers.forEach(user -> {
            String userId = user.getId();
            Map<String, List<String>> attributes = user.getAttributes() != null ? user.getAttributes() : Collections.emptyMap();
            List<String> roles = keycloak.realm(realm).users().get(userId).roles().realmLevel().listEffective()
                    .stream().map(RoleRepresentation::getName).collect(Collectors.toList());

            Map<String, Object> redisAttrs = new HashMap<>();
            attributes.forEach((k, v) -> redisAttrs.put(k, v.get(0)));
            redisAttrs.put("role", String.join("|", roles));
            abacPolicyService.updateUserAttributes(userId, redisAttrs);
        });
    }

    @Override
    public void syncUserFromKeycloak(String userId) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        Map<String, List<String>> attributes = user.getAttributes() != null ? user.getAttributes() : Collections.emptyMap();
        List<String> roles = keycloak.realm(realm).users().get(userId).roles().realmLevel().listEffective()
                .stream().map(RoleRepresentation::getName).collect(Collectors.toList());

        Map<String, Object> redisAttrs = new HashMap<>();
        attributes.forEach((k, v) -> redisAttrs.put(k, v.get(0)));
        redisAttrs.put("role", String.join("|", roles));
        abacPolicyService.updateUserAttributes(userId, redisAttrs);
    }

    public void updateUserAttributesInKeycloak(String userId, Map<String, Object> attributes) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        Map<String, List<String>> kcAttributes = new HashMap<>();
        attributes.forEach((k, v) -> kcAttributes.put(k, Collections.singletonList(v.toString())));
        user.setAttributes(kcAttributes);
        keycloak.realm(realm).users().get(userId).update(user);

        abacPolicyService.updateUserAttributes(userId, attributes);
    }
}