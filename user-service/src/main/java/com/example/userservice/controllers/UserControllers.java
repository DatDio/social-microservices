package com.example.userservice.controllers;

import com.example.userservice.entities.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserControllers {
    @GetMapping("/{id}")
    @PreAuthorize("@appAuthorizer.authorize(authentication, 'VIEW_DETAIL', #user)")
    public User getUser(@PathVariable String id) {
        User user = new User();
        user.setId(id);
        user.setStatus("active");
        return user;
    }
}
