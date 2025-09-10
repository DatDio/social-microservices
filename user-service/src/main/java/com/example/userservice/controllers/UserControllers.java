package com.example.userservice.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserControllers {
    @GetMapping("/me")
    public Map<String, String> getCurrentUser() {
        return Map.of(
                "message", "Request qua Gateway đã được xác thực, user-service chỉ xử lý logic!"
        );
    }
}
