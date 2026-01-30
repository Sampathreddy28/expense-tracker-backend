package com.ExpenseTracker.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities()
        ));
    }
}
