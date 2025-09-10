package com.example.authservice.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "permissions")
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private String description;
}
