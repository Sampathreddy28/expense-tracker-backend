package com.ExpenseTracker.service;

import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.Transaction.Type;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategorySummary;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendDto;
import com.ExpenseTracker.mod.dto.response.MonthlyTrendResponse;
import com.ExpenseTracker.mod.dto.response.SpendingInsightResponse;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calculates total spending grouped by category for a given period.
     * This data is used to generate Pie or Bar Charts for "Spending Breakdown".
     */
    public List<CategorySummary> getExpenseSummaryByCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        // Step 1: Fetch all EXPENSE transactions for the period
        List<Transaction> expenses = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
            .stream()
            .filter(t -> t.getType() == Type.EXPENSE)
            .collect(Collectors.toList());

        // Step 2: Group by category and sum the amounts
        return expenses.stream()
            .collect(Collectors.groupingBy(
                // Grouping key: Category name (or a default if null)
                t -> (t.getCategory() != null) ? t.getCategory().getName() : "Uncategorized", 
                // Aggregation: Sum the amount field
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ))
            .entrySet().stream()
            .map(entry -> new CategorySummary(entry.getKey(), entry.getValue()))
            .sorted((s1, s2) -> s2.getTotalAmount().compareTo(s1.getTotalAmount())) // Sort by highest spending
            .collect(Collectors.toList());
    }
    

    public List<MonthlyTrendResponse> getMonthlyTrends(User user, int months) {

        LocalDate startDate = LocalDate.now()
                .minusMonths(months - 1)
                .withDayOfMonth(1);

        return transactionRepository.findMonthlyExpenseTrends(
                user.getId(),
                startDate
        );
    }
    public List<SpendingInsightResponse> getSpendingInsights(User user) {

        LocalDate startDate = LocalDate.now()
                .minusMonths(3)
                .withDayOfMonth(1);

        List<Object[]> rawData =
                transactionRepository.findCategoryMonthlyTotals(
                        user.getId(), startDate);

        Map<String, Map<String, BigDecimal>> data = new HashMap<>();

        for (Object[] row : rawData) {
            String category = (String) row[0];
            String month = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            data.computeIfAbsent(category, k -> new HashMap<>())
                .put(month, amount);
        }

        List<SpendingInsightResponse> insights = new ArrayList<>();

        for (String category : data.keySet()) {
            Map<String, BigDecimal> monthly = data.get(category);

            List<String> months = monthly.keySet().stream().sorted().toList();

            if (months.size() >= 2) {
                BigDecimal prev = monthly.get(months.get(months.size() - 2));
                BigDecimal curr = monthly.get(months.get(months.size() - 1));

                if (prev.compareTo(BigDecimal.ZERO) > 0) {
                    int increase = curr
                            .subtract(prev)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(prev, 0, RoundingMode.HALF_UP)
                            .intValue();

                    if (increase >= 20) {
                        insights.add(new SpendingInsightResponse(
                                "WARNING",
                                category + " spending increased by " + increase + "% compared to last month"
                        ));
                    }
                }
            }

            if (months.size() >= 3) {
                BigDecimal m1 = monthly.get(months.get(months.size() - 3));
                BigDecimal m2 = monthly.get(months.get(months.size() - 2));
                BigDecimal m3 = monthly.get(months.get(months.size() - 1));

                if (m1.compareTo(m2) < 0 && m2.compareTo(m3) < 0) {
                    insights.add(new SpendingInsightResponse(
                            "ALERT",
                            category + " spending has increased for 3 consecutive months"
                    ));
                }
            }
        }

        return insights;
    }
    public List<MonthlyTrendDto> getMonthlyTrend(User user) {
        return transactionRepository.getMonthlyExpenseTrend(user.getId())
                .stream()
                .map(row -> new MonthlyTrendDto(
                        (String) row[0],
                        (BigDecimal) row[1]
                ))
                .toList();
    }
    public List<Object[]> getGlobalExpenseByCategory() {
        return transactionRepository.getGlobalExpenseByCategory(Transaction.Type.EXPENSE);
    }

    public Map<String, Double> getGlobalExpenseDistribution() {

        List<Object[]> results =
                transactionRepository.getGlobalExpenseByCategory(Transaction.Type.EXPENSE);

        Map<String, Double> expenseDistribution = new LinkedHashMap<>();

        for (Object[] row : results) {
            String category = (String) row[0];
            Double totalAmount = ((Number) row[1]).doubleValue();
            expenseDistribution.put(category, totalAmount);
        }

        return expenseDistribution;
    }



}