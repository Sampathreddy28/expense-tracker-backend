package com.ExpenseTracker.dao;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ExpenseTracker.mod.AdminTransactionSummary;
import com.ExpenseTracker.mod.Transaction;

public interface TransactionServiceRepo {
	Page<Transaction> getUserTransactions(
            Long userId,
            String search,
            Pageable pageable
    );

    // ✅ ADD THIS
    Page<Transaction> getAllTransactions(
            String search,
            Pageable pageable
    );
    
   
    public Page<Transaction> getAdminTransactions(
            int page,
            int size,
            String username,
            String search,
            String type,
            LocalDate startDate,
            LocalDate endDate
    );
    Page<Transaction> getAdminTransactions(int page, int size, String search, String type,
            String username, LocalDate startDate,
            LocalDate endDate, String sortBy, String direction);

AdminTransactionSummary getTotals();

List<Object[]> getMonthlyChartData();
}
