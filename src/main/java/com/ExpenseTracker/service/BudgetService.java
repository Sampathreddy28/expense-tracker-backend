package com.ExpenseTracker.service;

import com.ExpenseTracker.dao.BudgetRepository;
import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.Budget;
import com.ExpenseTracker.mod.Category;
import com.ExpenseTracker.mod.User;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepo;
    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;

    public BudgetService(
        BudgetRepository budgetRepo,
        TransactionRepository transactionRepo,
        UserRepository userRepo
    ) {
        this.budgetRepo = budgetRepo;
        this.transactionRepo = transactionRepo;
        this.userRepo = userRepo;
    }

    /* ================= USER ================= */

    public Long getUserIdByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow()
                .getId();
    }

    public List<Budget> getBudgetsByUser(Long userId) {
        return budgetRepo.findByUserId(userId);
    }

    /* ================= SET BUDGET ================= */

    public void setBudget(User user, Long categoryId, BigDecimal limit, YearMonth period) {
        Budget budget = budgetRepo
            .findByUserIdAndCategoryIdAndPeriod(user.getId(), categoryId, period)
            .orElse(new Budget());

        budget.setUser(user);
        budget.setCategory(new Category(categoryId));
        budget.setLimitAmount(limit);
        budget.setPeriod(period);

        budgetRepo.save(budget);
    }

    /* ================= ALERT CHECK ================= */

    public Optional<String> checkBudgetAlerts(
            Long userId,
            Long categoryId,
            YearMonth period
    ) {
        YearMonth targetPeriod = (period != null) ? period : YearMonth.now();

        Optional<Budget> budgetOpt =
            budgetRepo.findByUserIdAndCategoryIdAndPeriod(
                userId,
                categoryId,
                targetPeriod
            );

        if (budgetOpt.isEmpty()) {
            return Optional.empty(); // No budget set → no alert
        }

        Budget budget = budgetOpt.get();

        BigDecimal spent = transactionRepo
            .sumExpenseByCategoryAndMonth(
                userId,
                budget.getCategory().getId(),
                targetPeriod
            );

        BigDecimal limit = budget.getLimitAmount();

        if (spent.compareTo(limit) > 0) {
            return Optional.of(
                "🚨 Budget exceeded for " +
                budget.getCategory().getName() +
                " (" + spent + " / " + limit + ")"
            );
        }

        if (spent.compareTo(limit.multiply(BigDecimal.valueOf(0.8))) >= 0) {
            return Optional.of(
                "⚠ Warning: 80% of budget used for " +
                budget.getCategory().getName()
            );
        }

        return Optional.empty(); // Within limit
    }
    public Optional<String> checkBudgetAfterTransaction(
            Long userId,
            Long categoryId,
            YearMonth period
    ) {
        Optional<Budget> budgetOpt =
            budgetRepo.findByUserIdAndCategoryIdAndPeriod(
                userId, categoryId, period
            );

        if (budgetOpt.isEmpty()) return Optional.empty();

        Budget budget = budgetOpt.get();

        BigDecimal spent = budget.getSpentAmount();
        BigDecimal limit = budget.getLimitAmount();


        if (spent.compareTo(limit) >= 0) {
            return Optional.of("🚨 Budget limit exceeded!");
        }

        // ⚠️ spent >= 80% of limit
        BigDecimal warningLevel =
            limit.multiply(BigDecimal.valueOf(0.8));

        if (spent.compareTo(warningLevel) >= 0) {
            return Optional.of("⚠️ You have used 80% of your budget");
        }

        return Optional.empty();
    }
    @Transactional
    public void updateSpentAmount(
            Long userId,
            Long categoryId,
            YearMonth period,
            BigDecimal amount
    ) {
        Optional<Budget> budgetOpt =
            budgetRepo.findByUserIdAndCategoryIdAndPeriod(
                userId, categoryId, period
            );

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            budget.setSpentAmount(
                budget.getSpentAmount().add(amount)
            );
        }
    }

}
