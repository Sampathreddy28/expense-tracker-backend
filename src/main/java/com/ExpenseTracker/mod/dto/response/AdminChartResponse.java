package com.ExpenseTracker.mod.dto.response;


import java.math.BigDecimal;

public class AdminChartResponse {

    private String label;
    private BigDecimal income;
    private BigDecimal expense;
    private Long transactions;

    public AdminChartResponse(String label, BigDecimal income, BigDecimal expense, Long transactions) {
        this.label = label;
        this.income = income;
        this.expense = expense;
        this.transactions = transactions;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public Long getTransactions() {
        return transactions;
    }
}
