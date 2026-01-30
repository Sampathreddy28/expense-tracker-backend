package com.ExpenseTracker.mod.dto;

import java.math.BigDecimal;

public record CategoryAnalyticsDto(
        String category,
        BigDecimal total
) {}
