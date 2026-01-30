package com.ExpenseTracker.mod.dto.response;

import java.math.BigDecimal;

public record MonthlyTrendResponse(
        String month,     // e.g. 2024-08
        BigDecimal amount
) {}