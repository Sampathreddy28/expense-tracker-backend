package com.ExpenseTracker.mod.dto.response;


import java.math.BigDecimal;

public class BudgetAlertResponse {

    private String categoryName;
    private BigDecimal limitAmount;
    private BigDecimal currentSpending;
    private BigDecimal exceededBy;

    public BudgetAlertResponse() {
    }

    public BudgetAlertResponse(
            String categoryName,
            BigDecimal limitAmount,
            BigDecimal currentSpending
    ) {
        this.categoryName = categoryName;
        this.limitAmount = limitAmount;
        this.currentSpending = currentSpending;
        this.exceededBy = currentSpending.subtract(limitAmount);
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public BigDecimal getCurrentSpending() {
        return currentSpending;
    }

    public void setCurrentSpending(BigDecimal currentSpending) {
        this.currentSpending = currentSpending;
    }

    public BigDecimal getExceededBy() {
        return exceededBy;
    }

    public void setExceededBy(BigDecimal exceededBy) {
        this.exceededBy = exceededBy;
    }
}
