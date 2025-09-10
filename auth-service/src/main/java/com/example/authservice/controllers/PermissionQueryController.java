package com.example.authservice.controllers;

import com.example.authservice.services.RolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/internal/permissions")
@RequiredArgsConstructor
public class PermissionQueryController {

    private final RolePermissionService service;

    @GetMapping
    public Set<String> getPermissions(@RequestParam List<String> roles) {
        return service.getPermissionsForRoles(roles);
    }
}
