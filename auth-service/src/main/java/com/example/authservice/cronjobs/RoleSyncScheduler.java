package com.example.authservice.cronjobs;

import com.example.authservice.services.Implements.KeycloakServiceImpl;
import com.example.authservice.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class RoleSyncScheduler {
//    private final KeycloakServiceImpl keycloakService;
//
//    public void syncRoles() {
//        keycloakService.syncUsersFromKeycloak();
//    }
}
