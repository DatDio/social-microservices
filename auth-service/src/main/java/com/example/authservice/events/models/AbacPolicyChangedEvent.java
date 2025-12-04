package com.example.authservice.events.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class AbacPolicyChangedEvent {
    private String service;
    private String function;
    private Map<String, Object> conditions;
}