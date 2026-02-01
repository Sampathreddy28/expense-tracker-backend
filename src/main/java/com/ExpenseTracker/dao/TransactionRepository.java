package com.ExpenseTracker.dao;

import com.ExpenseTracker.mod.Category;

import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.Transaction.Type;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategorySummary;
import com.ExpenseTracker.mod.dto.response.AdminMonthlyTrendDto;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendDto;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendResponse;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	// Fetches all transactions for a specific user, sorted by date (Transaction
	// History)
	List<Transaction> findByUserIdOrderByDateDesc(Long userId);

	// Custom query to calculate the sum of amounts for a specific user and type
	// (e.g., all INCOME)
	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
	BigDecimal sumAmountByUserIdAndType(Long userId, Type type);

	// Fetches transactions for reporting (Monthly/Yearly Reports)
	List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

	@Query("SELECT SUM(t.amount) " + "FROM Transaction t " + "WHERE t.user.id = :userId " + "  AND t.type = :type "
			+ "  AND t.category = :category " + // Filter by the Category Entity object
			"  AND t.date BETWEEN :startDate AND :endDate")
	BigDecimal sumAmountByUserIdAndTypeAndCategory(Long userId, Type type, Category category, // Note: We filter by the
																								// Category OBJECT, not
																								// just its ID
			LocalDate startDate, LocalDate endDate);

	// Inside TransactionRepository.java

	@Query("""
    SELECT FUNCTION('YEAR', t.date),
           FUNCTION('MONTH', t.date),
           SUM(t.amount)
    FROM Transaction t
    WHERE t.user.id = :userId AND t.type = :type
    GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
    ORDER BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
""")
List<Object[]> getMonthlySummaryByUserIdAndType(Long userId, Type type);


	/**
	 * FIX: Defines the custom aggregation query to calculate Net Balance. Net
	 * Balance = SUM(Income) - SUM(Expense)
	 */
	@Query("SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END) "
			+ "FROM Transaction t WHERE t.user.id = :userId")
	BigDecimal calculateNetBalanceByUserId(@Param("userId") Long userId);

	void deleteByIdAndUserId(Long transactionId, Long userId);

	@Query("""
			    SELECT c.name, SUM(t.amount)
			    FROM Transaction t
			    JOIN t.category c
			    WHERE t.user.id = :userId
			    GROUP BY c.name
			""")
	List<Object[]> getCategorySummary(@Param("userId") Long userId);

	@Query("""
			    SELECT
			        FUNCTION('MONTH', t.date),
			        FUNCTION('YEAR', t.date),
			        SUM(t.amount)
			    FROM Transaction t
			    WHERE t.user.id = :userId
			      AND t.type = 'EXPENSE'
			    GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
			    ORDER BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
			""")
	List<Object[]> getMonthlyExpenseSummary(@Param("userId") Long userId);

	@Query("""
			SELECT MONTH(t.date), SUM(t.amount)
			FROM Transaction t
			WHERE t.user.id = :userId AND t.type = 'EXPENSE'
			GROUP BY MONTH(t.date)
			""")
	List<Object[]> monthlyExpenseSummary(Long userId);

	@Query("""
			    SELECT t
			    FROM Transaction t
			    WHERE t.user.id = :userId
			""")
	List<Transaction> findByUserId(Long userId);

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'INCOME'")
	BigDecimal totalIncome();

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'EXPENSE'")
	BigDecimal totalExpenses();

	@Query("""
    SELECT new com.ExpenseTracker.mod.dto.response.MonthlyTrendResponse(
        FUNCTION('YEAR', t.date),
        FUNCTION('MONTH', t.date),
        COALESCE(SUM(t.amount), 0)
    )
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = 'EXPENSE'
      AND t.date >= :startDate
    GROUP BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
    ORDER BY FUNCTION('YEAR', t.date), FUNCTION('MONTH', t.date)
""")
List<MonthlyTrendResponse> findMonthlyExpenseTrends(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate);



@Query("""
    SELECT t.category.name,
           FUNCTION('YEAR', t.date),
           FUNCTION('MONTH', t.date),
           COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = 'EXPENSE'
      AND t.date >= :startDate
    GROUP BY t.category.name,
             FUNCTION('YEAR', t.date),
             FUNCTION('MONTH', t.date)
""")
	List<Object[]> findCategoryMonthlyTotals(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

	List<Transaction> findByUser(User user);

	@Query("""
			    SELECT
			        MONTH(t.date),
			        SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END),
			        SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END),
			        COUNT(t)
			    FROM Transaction t
			    GROUP BY MONTH(t.date)
			    ORDER BY MONTH(t.date)
			""")
	List<Object[]> monthlyAnalytics();

	@Query("""
			 SELECT COALESCE(SUM(t.amount),0)
			 FROM Transaction t
			 WHERE t.user = :user
			   AND t.category = :category
			   AND t.type = 'EXPENSE'
			   AND t.date >= :start
			   AND t.date < :end
			""")
	BigDecimal spentForCategory(User user, Category category, LocalDate start, LocalDate end);

	@Query("""
			    SELECT c.name, SUM(t.amount)
			    FROM Transaction t
			    JOIN t.category c
			    WHERE t.type = 'EXPENSE'
			    GROUP BY c.name
			""")
	List<Object[]> getExpenseByCategory();

	@Query(value = """
    SELECT EXTRACT(YEAR FROM t.date) AS year,
           EXTRACT(MONTH FROM t.date) AS month,
           SUM(t.amount)
    FROM transactions t
    WHERE t.type = 'EXPENSE'
      AND t.user_id = :userId
    GROUP BY year, month
    ORDER BY year, month
""", nativeQuery = true)
List<Object[]> getMonthlyExpenseTrend(@Param("userId") Long userId);
	

	@Query("""
			    SELECT t.category, SUM(t.amount)
			    FROM Transaction t
			    WHERE t.type = :type
			    GROUP BY t.category
			""")
	List<Object[]> getGlobalExpenseByCategory(@Param("type") Transaction.Type type);

	Page<Transaction> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String search, Pageable pageable);

	// USER search
	@Query("""
			    SELECT t FROM Transaction t
			    WHERE t.user.id = :userId
			    AND (
			        LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
			        OR LOWER(t.category.name) LIKE LOWER(CONCAT('%', :search, '%'))
			    )
			""")
	Page<Transaction> searchUserTransactions(Long userId, String search, Pageable pageable);

	// ADMIN search
	@Query("""
			    SELECT t FROM Transaction t
			    WHERE
			        LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
			        OR LOWER(t.category.name) LIKE LOWER(CONCAT('%', :search, '%'))
			""")
	Page<Transaction> searchAll(String search, Pageable pageable);

	Page<Transaction> findByUserId(Long userId, Pageable pageable);

	@Query("""
			SELECT COALESCE(SUM(t.amount), 0)
			FROM Transaction t
			WHERE t.user.id = :userId
			  AND t.category.id = :categoryId
			  AND t.type = 'EXPENSE'
			  AND FUNCTION('YEAR', t.date) = :year
			  AND FUNCTION('MONTH', t.date) = :month
			""")
	BigDecimal sumExpenseByCategoryAndMonth(Long userId, Long categoryId, int year, int month);

	default BigDecimal sumExpenseByCategoryAndMonth(Long userId, Long categoryId, YearMonth period) {
		return sumExpenseByCategoryAndMonth(userId, categoryId, period.getYear(), period.getMonthValue());
	}

// 	@Query("""
// 			    SELECT COALESCE(SUM(t.amount), 0)
// 			    FROM Transaction t
// 			    WHERE t.user.id = :userId
// 			      AND t.category.id = :categoryId
// 			     AND FUNCTION('YEAR', t.date) = :year
// AND FUNCTION('MONTH', t.date) = :month
// 			""")
// 	BigDecimal getSpentAmount(Long userId, Long categoryId, YearMonth period);

	@Query("""
			SELECT YEAR(t.date), MONTH(t.date), SUM(t.amount)
			FROM Transaction t
			WHERE t.user.id = :userId AND t.type = 'EXPENSE'
			GROUP BY YEAR(t.date), MONTH(t.date)
			""")
	List<Object[]> monthlyExpenseRaw(@Param("userId") Long userId);

	default Map<YearMonth, BigDecimal> findMonthlyExpense(Long userId) {
		return monthlyExpenseRaw(userId).stream()
				.collect(Collectors.toMap(r -> YearMonth.of((int) r[0], (int) r[1]), r -> (BigDecimal) r[2]));
	}

	@Query("""
			SELECT c.name, SUM(t.amount)
			FROM Transaction t
			JOIN t.category c
			WHERE t.user.id = :userId
			GROUP BY c.name
			""")
	List<Object[]> findCategoryTotals(Long userId);

	@Query("""
			SELECT t FROM Transaction t
			WHERE t.user.id = :userId
			AND (:search IS NULL OR LOWER(t.description) LIKE %:search%)
			AND (:type IS NULL OR t.type = :type)
			AND (:categoryId IS NULL OR t.category.id = :categoryId)
			AND t.date BETWEEN :startDate AND :endDate
			""")
	Page<Transaction> findFilteredTransactions(Long userId, String search, Transaction.Type type, Long categoryId,
			LocalDate startDate, LocalDate endDate, Pageable pageable);

	@Query("""
			SELECT t FROM Transaction t
			WHERE (:username IS NULL OR t.user.username = :username)
			AND (:type IS NULL OR t.type = :type)
			AND (:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
			AND (:startDate IS NULL OR t.date >= :startDate)
			AND (:endDate IS NULL OR t.date <= :endDate)
			""")
	Page<Transaction> findAdminTransactions(@Param("username") String username, @Param("type") String type,
			@Param("search") String search, @Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate, Pageable pageable);

	Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
	
	@Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type")
	Double sumByType(@Param("type") Transaction.Type type);

	
@Query("""
SELECT EXTRACT(MONTH FROM t.date),
       SUM(CASE WHEN t.type='INCOME' THEN t.amount ELSE 0 END),
       SUM(CASE WHEN t.type='EXPENSE' THEN t.amount ELSE 0 END)
FROM Transaction t
GROUP BY EXTRACT(MONTH FROM t.date)
ORDER BY 1
""")
List<Object[]> getMonthlySummary();







		    // ADMIN analytics (income vs expense)
		    @Query("""
		        select new com.ExpenseTracker.mod.dto.response.AdminMonthlyTrendDto(
		            month(t.date),
		            sum(case when t.type = 'INCOME' then t.amount else 0 end),
		            sum(case when t.type = 'EXPENSE' then t.amount else 0 end),
		            count(t.id)
		        )
		        from Transaction t
		        group by month(t.date)
		        order by month(t.date)
		    """)
		    List<MonthlyTrendDto> monthlyTrendAllUsers();


		    @Query("""
		        select new com.ExpenseTracker.mod.dto.response.AdminMonthlyTrendDto(
		            month(t.date),
		            sum(case when t.type = 'INCOME' then t.amount else 0 end),
		            sum(case when t.type = 'EXPENSE' then t.amount else 0 end),
		            count(t.id)
		        )
		        from Transaction t
		        where t.user.username = :username
		        group by month(t.date)
		        order by month(t.date)
		    """)
		    List<MonthlyTrendDto> monthlyTrendByUser(@Param("username") String username);
		


}
