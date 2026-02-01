package com.ExpenseTracker.dao;

import com.ExpenseTracker.mod.Budget;
import com.ExpenseTracker.mod.Category;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.response.BudgetResponse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    List<Budget> findByUserIdAndPeriod(Long userId, YearMonth period);

    Optional<Budget> findByUserIdAndCategoryIdAndPeriod(
        Long userId,
        Long categoryId,
        YearMonth period
    );

    Optional<Budget> findByUserAndCategoryAndPeriod(
        User user,
        Category category,
        YearMonth period
    );

    
    @Query("""
SELECT c.name,
       b.limitAmount,
       COALESCE(SUM(t.amount),0),
       (b.limitAmount - COALESCE(SUM(t.amount),0)),
       CASE WHEN COALESCE(SUM(t.amount),0) > b.limitAmount
            THEN true ELSE false END
FROM Budget b
JOIN b.category c
LEFT JOIN Transaction t
  ON t.category.id = c.id
 AND t.user.id = b.user.id
 AND t.type = 'EXPENSE'
 AND EXTRACT(YEAR FROM t.date) = :year
 AND EXTRACT(MONTH FROM t.date) = :month
WHERE b.user.id = :userId
AND b.period = :period
GROUP BY c.name, b.limitAmount
""")
    List<BudgetResponse> findBudgetsByUser(
    		    @Param("userId") Long userId,
    		    @Param("period") YearMonth period,
    		    @Param("year") int year,
    		    @Param("month") int month
    		);



}
