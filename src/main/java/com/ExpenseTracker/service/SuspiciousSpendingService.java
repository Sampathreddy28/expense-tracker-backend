package com.ExpenseTracker.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class SuspiciousSpendingService {

    public boolean isSuspicious(BigDecimal amount) {

        // ✅ Basic fraud rules
        if (amount.compareTo(BigDecimal.valueOf(5000)) > 0) {
            return true; // big expense
        }

        return false;
    }
}
