package com.ExpenseTracker.controller;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ExpenseTracker.dao.AdminService;
import com.ExpenseTracker.mod.CustomUserDetails;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategorySummary;
import com.ExpenseTracker.mod.dto.response.AdminChartResponse;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendDto;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendResponse;
import com.ExpenseTracker.service.AnalyticsService;
import com.ExpenseTracker.service.UserService;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;
    private final AdminService adminService; 
    public AdminAnalyticsController(
            AnalyticsService analyticsService,
            UserService userService, AdminService adminService) {
        this.analyticsService = analyticsService;
        this.userService = userService;
		this.adminService = adminService;
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendDto>> monthlyTrend(
            @AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(
                analyticsService.getMonthlyTrend(user.getUser())
        );
    }


    @GetMapping("/category-distribution")
    public ResponseEntity<Map<String, Double>> categoryDistributionAllUsers() {
        return ResponseEntity.ok(
            analyticsService.getGlobalExpenseDistribution()
        );
    }
    @GetMapping("/category-summary")
    public ResponseEntity<List<CategorySummary>> categorySummaryByUser(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        return ResponseEntity.ok(
            analyticsService.getExpenseSummaryByCategory(
                user.getId(), startDate, endDate
            )
        );
    }
    @GetMapping("/income-expense")
    public ResponseEntity<List<AdminChartResponse>> incomeExpense() {
        return ResponseEntity.ok(adminService.getMonthlyAnalytics());
    }

}
