package com.example.authservice.controllers;

import com.example.authservice.services.AbacPolicyService;
import com.example.authservice.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AbacPolicyService abacPolicyService;
    private final KeycloakService keycloakService;

    @PostMapping("/policy/{service}/{function}")
    public ResponseEntity<Void> updatePolicy(
            @PathVariable String service,
            @PathVariable String function,
            @RequestBody Map<String, Object> conditions) {
        abacPolicyService.updatePolicy(service, function, conditions);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{userId}/attributes")
    public ResponseEntity<Void> updateUserAttributes(
            @PathVariable String userId,
            @RequestBody Map<String, Object> attributes) {
        keycloakService.updateUserAttributesInKeycloak(userId, attributes);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/abac/evaluate")
    public ResponseEntity<Boolean> evaluateAbac(@RequestBody AbacRequest request) {
        boolean allowed = abacPolicyService.evaluatePolicy(
                request.getService(), request.getFunction(), request.getUserId(), request.getRequestAttributes()
        );
        return ResponseEntity.ok(allowed);
    }

    public static class AbacRequest {
        private String service, function, userId;
        private Map<String, Object> requestAttributes;

        public String getService() { return service; }
        public void setService(String service) { this.service = service; }
        public String getFunction() { return function; }
        public void setFunction(String function) { this.function = function; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public Map<String, Object> getRequestAttributes() { return requestAttributes; }
        public void setRequestAttributes(Map<String, Object> requestAttributes) { this.requestAttributes = requestAttributes; }
    }
}
