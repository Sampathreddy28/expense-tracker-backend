package com.ExpenseTracker.mod.dto.response;

import java.math.BigDecimal;

public class AdminMonthlyTrendDto {

    private Integer month;
    private BigDecimal income;
    private BigDecimal expense;
    private Long transactionCount;

    public AdminMonthlyTrendDto(
            Integer month,
            BigDecimal income,
            BigDecimal expense,
            Long transactionCount
    ) {
        this.month = month;
        this.income = income;
        this.expense = expense;
        this.transactionCount = transactionCount;
    }

    public Integer getMonth() {
        return month;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }
}
