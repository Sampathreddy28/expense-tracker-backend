package com.ExpenseTracker.controller;

import com.ExpenseTracker.dao.BudgetService;
import com.ExpenseTracker.dao.request.BudgetRequest;
import com.ExpenseTracker.mod.Budget;
import com.ExpenseTracker.mod.CustomUserDetails;
import com.ExpenseTracker.mod.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

// Assume BudgetRequest DTO exists to handle input for Budget entity

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // extracted from JWT
        return budgetService.getUserIdByUsername(username);
    }

    @GetMapping("")
    public ResponseEntity<?> getBudgets() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(budgetService.getBudgetsByUser(userId));
    }

    @GetMapping("/alerts")
    public ResponseEntity<?> getBudgetAlerts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) YearMonth period) {

        Long userId = getCurrentUserId();

        Optional<String> alertMessage =
                budgetService.checkBudgetAlerts(userId, categoryId, period);

        if (alertMessage.isPresent()) {
            return ResponseEntity.status(200).body(
                Map.of(
                    "status", "ALERT",
                    "message", alertMessage.get()
                )
            );
        }

        return ResponseEntity.ok(
            Map.of(
                "status", "OK",
                "message", "Budget is currently within limit"
            )
        );
    }


      
    @PostMapping
    public ResponseEntity<?> setBudget(
        @RequestBody BudgetRequest req,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        budgetService.setBudget(
            user.getUser(),
            req.categoryId(),
            req.limit(),
            req.period()
        );
        return ResponseEntity.ok().build();
    }

}