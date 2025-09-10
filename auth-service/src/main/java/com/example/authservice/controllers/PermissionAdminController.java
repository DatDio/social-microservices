package com.example.authservice.controllers;

import com.example.authservice.services.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('Admin_Group')")
public class PermissionAdminController {

    private final RolePermissionService service;

    @PostMapping("/assign")
    public ResponseEntity<?> assignPermission(
            @RequestParam String roleName,
            @RequestParam String permName) {
        service.assignPermission(roleName, permName);
        return ResponseEntity.ok("Permission assigned");
    }

    @PostMapping("/revoke")
    public ResponseEntity<?> revokePermission(
            @RequestParam String roleName,
            @RequestParam String permName) {
        service.revokePermission(roleName, permName);
        return ResponseEntity.ok("Permission revoked");
    }
}
