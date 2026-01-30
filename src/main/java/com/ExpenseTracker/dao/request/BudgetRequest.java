package com.ExpenseTracker.dao.request;

import java.math.BigDecimal;
import java.time.YearMonth;

public record BudgetRequest(
        Long categoryId,
        BigDecimal limit,
        YearMonth period
) {}
