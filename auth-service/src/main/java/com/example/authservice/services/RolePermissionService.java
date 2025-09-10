package com.example.authservice.services;

import com.example.authservice.entities.PermissionEntity;
import com.example.authservice.entities.RoleEntity;
import com.example.authservice.events.models.PermissionChangedEvent;
import com.example.authservice.events.producers.PermissionEventPublisher;
import com.example.authservice.repositories.PermissionRepository;
import com.example.authservice.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
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
    private final PermissionEventPublisher eventPublisher;

    public void assignPermission(String roleName, String permName) {
        RoleEntity role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        PermissionEntity perm = permRepo.findByName(permName)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        role.getPermissions().add(perm);

        roleRepo.save(role);

        eventPublisher.publish(new PermissionChangedEvent(roleName, List.of(permName), "ASSIGN"));
    }

    public void revokePermission(String roleName, String permName) {
        RoleEntity role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.getPermissions().removeIf(p -> p.getName().equalsIgnoreCase(permName));
        roleRepo.save(role);

        eventPublisher.publish(new PermissionChangedEvent(roleName, List.of(permName), "REVOKE"));
    }

    public Set<String> getPermissionsForRoles(Collection<String> roles) {
        return roleRepo.findPermissionsByRoleNames(roles);
    }
}
