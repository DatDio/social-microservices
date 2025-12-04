package com.example.authservice.services;

import com.example.authservice.entities.PermissionEntity;
import com.example.authservice.entities.RoleEntity;
import com.example.authservice.events.models.PermissionChangedEvent;
import com.example.authservice.events.producers.PermissionEventPublisher;
import com.example.authservice.repositories.PermissionRepository;
import com.example.authservice.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;
    private final StringRedisTemplate redisTemplate; // Spring Data Redis

    private static final String PERM_KEY_PREFIX = "role_permissions:";

    public void assignPermission(String roleName, String permName) {
        RoleEntity role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        PermissionEntity perm = permRepo.findByName(permName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.getPermissions().add(perm);
        roleRepo.save(role);

        // update redis cache
        String key = PERM_KEY_PREFIX + roleName;
        redisTemplate.opsForSet().add(key, perm.getName());
    }

    public void revokePermission(String roleName, String permName) {
        RoleEntity role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.getPermissions().removeIf(p -> p.getName().equalsIgnoreCase(permName));
        roleRepo.save(role);

        // remove from redis cache
        String key = PERM_KEY_PREFIX + roleName;
        redisTemplate.opsForSet().remove(key, permName);
    }
}
