package com.ExpenseTracker.mod.dto.response;


import java.math.BigDecimal;
import java.time.YearMonth;
public record BudgetResponse(
	    String category,
	    BigDecimal limit,
	    BigDecimal spent,
	    BigDecimal remaining,
	    boolean exceeded
	) {}
