package com.ExpenseTracker.mod;

import jdk.jfr.DataAmount;

@DataAmount

public class AdminTransactionSummary {
	private Double totalIncome;
    private Double totalExpense;
	public AdminTransactionSummary(Double totalIncome, Double totalExpense) {
		super();
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
	}
	public AdminTransactionSummary() {
		super();
	}
    
    
}
