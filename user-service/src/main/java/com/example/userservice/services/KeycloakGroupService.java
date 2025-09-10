package com.example.userservice.services;

import jakarta.ws.rs.NotFoundException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeycloakGroupService {

    private final Keycloak keycloak;
    private final String realm = "demo-realm";

    public KeycloakGroupService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public void addUserToGroup(String userId, String groupName) {
        // Lấy danh sách group
        List<GroupRepresentation> groups = keycloak.realm(realm).groups().groups();

        // Tìm group theo tên
        GroupRepresentation group = groups.stream()
                .filter(g -> g.getName().equals(groupName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Group not found: " + groupName));

        // Thêm user vào group
        keycloak.realm(realm).users()
                .get(userId)
                .joinGroup(group.getId());
    }

    public void removeUserFromGroup(String userId, String groupName) {
        List<GroupRepresentation> groups = keycloak.realm(realm).groups().groups();

        GroupRepresentation group = groups.stream()
                .filter(g -> g.getName().equals(groupName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Group not found: " + groupName));

        keycloak.realm(realm).users()
                .get(userId)
                .leaveGroup(group.getId());
    }
}
