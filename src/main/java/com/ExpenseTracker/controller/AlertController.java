package com.ExpenseTracker.controller;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.service.AlertService;

import java.util.Map;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final UserRepository userRepository;

    public AlertController(AlertService alertService, UserRepository userRepository) {
        this.alertService = alertService;
        this.userRepository = userRepository;
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTestAlert(Authentication auth) {
        try {
            User user = userRepository
                .findByUsername(auth.getName())
                .orElseThrow();

            alertService.sendAlert(
                user,
                "✅ This is a test expense alert from ExpenseTracker."
            );

            return ResponseEntity.ok(
                Map.of("status", "SUCCESS", "message", "✅ Test email sent successfully!")
            );

        } catch (Exception e) {
            return ResponseEntity
                .status(500)
                .body(Map.of("status", "FAILED", "message", "❌ Failed to send test email", "error", e.getMessage()));
        }
    }
}
