package com.ExpenseTracker.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ExpenseTracker.mod.AdminTransactionSummary;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.security.services.TransactionServiceImpl;
import com.ExpenseTracker.service.TransactionService;

@RestController
@RequestMapping("/api/transactions/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransactionController {

    private final TransactionService transactionService;
    private final TransactionServiceImpl transactionServiceImpl;
    public AdminTransactionController(TransactionService transactionService, TransactionServiceImpl transactionServiceImpl) {
        this.transactionService = transactionService;
		this.transactionServiceImpl = transactionServiceImpl;
    }

    // ✅ Paginated & filtered transactions
    @GetMapping
    public Page<Transaction> getAdminTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        return transactionService.getAdminTransactions(
                page, size, search, type, username,
                startDate, endDate, sortBy, direction
        );
    }

    // ✅ Summary totals
    @GetMapping("/summary")
    public AdminTransactionSummary summary() {
        return transactionService.getTotals();
    }

    // ✅ Monthly chart data
    @GetMapping("/chart")
    public List<Object[]> chart() {
        return transactionServiceImpl.getMonthlyChartData();
    }
}
