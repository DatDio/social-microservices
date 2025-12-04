package com.example.authservice.events.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class UserAttributesChangedEvent {
    private String userId;
    private Map<String, Object> attributes;
}
