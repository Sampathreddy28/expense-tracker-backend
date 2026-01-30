package com.ExpenseTracker.dao;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.ExpenseTracker.mod.Budget;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.response.BudgetResponse;

public interface BudgetService {

    Budget saveBudget(Budget budget);

    List<BudgetResponse> getBudgetsByUser(Long userId);

    Optional<String> checkBudgetAlerts(
            Long userId,
            Long categoryId,
            YearMonth period
    );
    Long getUserIdByUsername(String username);

	void setBudget(User user, Long categoryId, BigDecimal limit, YearMonth period);
	
	List<BudgetResponse> getBudgetsByUser(User user);

}