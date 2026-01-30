package com.ExpenseTracker.controller;

import com.ExpenseTracker.mod.CustomUserDetails;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategorySummary;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendDto;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendResponse;
import com.ExpenseTracker.mod.dto.response.SpendingInsightResponse;
import com.ExpenseTracker.service.AnalyticsService;
import com.ExpenseTracker.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;

    public AnalyticsController(AnalyticsService analyticsService, UserService userService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
    }

    @GetMapping("/category-summary")
    public ResponseEntity<List<CategorySummary>> getCategorySummary(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        Long userId = user.getUser().getId();
        return ResponseEntity.ok(
                analyticsService.getExpenseSummaryByCategory(userId, startDate, endDate)
        );
    }

    @GetMapping("/trends")
    public ResponseEntity<List<MonthlyTrendResponse>> getTrends(
            @RequestParam(defaultValue = "6") int months,
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                analyticsService.getMonthlyTrends(user.getUser(), months)
        );
    }



    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendDto>> monthlyTrend(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                analyticsService.getMonthlyTrend(user.getUser())
        );
    }
}
