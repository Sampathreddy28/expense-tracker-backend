package com.ExpenseTracker.security.services;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ExpenseTracker.dao.BudgetRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.Budget;
import com.ExpenseTracker.mod.Category;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.response.BudgetResponse;

import jakarta.transaction.Transactional;

import com.ExpenseTracker.dao.BudgetService;
import com.ExpenseTracker.dao.CategoryRepository;

@Service
public class BudgetServiceImpl implements BudgetService {
	@Override
	public List<BudgetResponse> getBudgetsByUser(Long userId) {
	    YearMonth period = YearMonth.now();

	    return budgetRepository.findBudgetsByUser(
	            userId,
	            period,
	            period.getYear(),
	            period.getMonthValue()
	    );
	}

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final  CategoryRepository categoryRepo;
    public BudgetServiceImpl(
            BudgetRepository budgetRepository,
            UserRepository userRepository, CategoryRepository categoryRepo) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
		this.categoryRepo = categoryRepo;
    }

    @Override
    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + username))
                .getId();
    }

    @Override
    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

   
  
    @Override
    @Transactional
    public void setBudget(
            User user,
            Long categoryId,
            BigDecimal limit,
            YearMonth period
    ) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow();

        Budget budget = budgetRepository
            .findByUserAndCategoryAndPeriod(user, category, period)
            .orElse(new Budget());

        budget.setUser(user);
        budget.setCategory(category);
        budget.setLimitAmount(limit);
        budget.setPeriod(period);

        budgetRepository.save(budget);
    }
    @Override
    public Optional<String> checkBudgetAlerts(
            Long userId,
            Long categoryId,
            YearMonth period) {

        Optional<Budget> budgetOpt =
            budgetRepository.findByUserIdAndCategoryIdAndPeriod(
                userId, categoryId, period
            );

        if (budgetOpt.isEmpty()) {
            return Optional.empty();
        }

        Budget budget = budgetOpt.get();

        BigDecimal spent = budget.getSpentAmount(); // or calculate
        BigDecimal limit = budget.getLimitAmount();

        if (spent.compareTo(limit) >= 0) {
            return Optional.of("⚠ Budget exceeded!");
        }

        if (spent.compareTo(limit.multiply(BigDecimal.valueOf(0.8))) >= 0) {
            return Optional.of("⚠ You have used 80% of your budget");
        }

        return Optional.empty();
    }
    @Override
    public List<BudgetResponse> getBudgetsByUser(User user) {
        YearMonth period = YearMonth.now();

        return budgetRepository.findBudgetsByUser(
                user.getId(),
                period,
                period.getYear(),
                period.getMonthValue()
        );
    }

}
