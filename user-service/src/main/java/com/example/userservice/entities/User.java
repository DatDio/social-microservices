package com.example.userservice.entities;

import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserProfile {
    @Id
    private String userId; // UUID từ Keycloak
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String nickname;
    private String bio;
    private String avatar;
    private LocalDate dateOfBirth;
    private String location;
    private String role; // Từ Keycloak attributes hoặc roles
    private String status; // Từ Keycloak attributes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
