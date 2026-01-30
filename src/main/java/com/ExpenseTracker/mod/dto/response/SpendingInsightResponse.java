package com.ExpenseTracker.mod.dto.response;

public record SpendingInsightResponse(
        String type,     // INFO, WARNING, ALERT
        String message
) {}
