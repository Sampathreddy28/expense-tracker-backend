// BalanceResponse.java (New File)

package com.ExpenseTracker.mod.dto.response; // Or similar DTO package

import java.math.BigDecimal;

public class BalanceResponse {
    private final BigDecimal balance;

    public BalanceResponse(BigDecimal balance) {
        this.balance = balance;
    }

    // CRITICAL: Need a getter for Jackson (Spring's JSON library) to serialize
    public BigDecimal getBalance() {
        return balance;
    }
}