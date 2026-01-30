package com.ExpenseTracker.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ExpenseTracker.mod.dto.CategoryAnalyticsDto;
import com.ExpenseTracker.mod.dto.response.AdminChartResponse;
import com.ExpenseTracker.mod.dto.response.AdminMetricsResponse;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendDto;
public interface AdminService {

    void promoteUserToAdmin(Long userId);

    AdminMetricsResponse getMetrics();

    List<AdminChartResponse> getMonthlyAnalytics();

//    void revokeAllTokensForUser(String username);

    List<CategoryAnalyticsDto> getCategoryAnalytics();

    // ✅ clean service methods
    List<MonthlyTrendDto> monthlyTrendAllUsers();

    List<MonthlyTrendDto> monthlyTrendByUser(String username);
   
}
