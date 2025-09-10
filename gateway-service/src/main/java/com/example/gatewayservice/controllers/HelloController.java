package com.example.gatewayservice.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Gateway is running - you need to login!";
    }

    @GetMapping("/secure")
    public String secure(@AuthenticationPrincipal Jwt jwt) {
        return "Hello, " + jwt.getClaimAsString("preferred_username");
    }
}