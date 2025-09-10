package com.example.authservice.events.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionChangedEvent {
    private String roleName;
    private List<String> changedPermissions;
    private String action; // "ASSIGN" hoặc "REVOKE"
}
