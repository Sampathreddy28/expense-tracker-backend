package com.ExpenseTracker.mod.dto.response;

import java.math.BigDecimal;

public class MonthlyTrendDto {

    private String month;
    private BigDecimal amount;

    public MonthlyTrendDto(String month, BigDecimal amount) {
        this.month = month;
        this.amount = amount;
    }

   

	public String getMonth() {
        return month;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
