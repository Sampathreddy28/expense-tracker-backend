package com.ExpenseTracker.mod.dto.response;

import java.math.BigDecimal;

public record AdminMetricsResponse(
        long totalUsers,
        long activeUsers,
        long totalTransactions,
        BigDecimal totalIncome,
        BigDecimal totalExpenses
) {}
