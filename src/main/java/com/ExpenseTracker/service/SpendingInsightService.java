package com.ExpenseTracker.service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ExpenseTracker.mod.User;

@Service
public class SpendingInsightService {

    private final AlertService alertService;

    public SpendingInsightService(AlertService alertService) {
        this.alertService = alertService;
    }

        public List<String> generateInsights(
                Map<YearMonth, BigDecimal> monthlyExpense,
                Map<String, BigDecimal> categoryTotals
        ) {
            List<String> insights = new ArrayList<>();

            // ✅ 1) Find highest category spend
            String topCategory = null;
            BigDecimal topAmount = BigDecimal.ZERO;

            for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
                if (entry.getValue().compareTo(topAmount) > 0) {
                    topAmount = entry.getValue();
                    topCategory = entry.getKey();
                }
            }

            if (topCategory != null) {
                insights.add("📌 Your top spending category is *" + topCategory +
                        "* with ₹" + topAmount + ".");
            }

            // 🔴 2) High category spending warning
            categoryTotals.forEach((category, amount) -> {
                if (amount.compareTo(BigDecimal.valueOf(8000)) > 0) {
                    insights.add("⚠ Heavy spending on *" + category +
                            "* (₹" + amount + "). Try reducing it next month.");
                }
            });

            // 📈 3) Spending trend detection (last 3 months)
            List<BigDecimal> values = new ArrayList<>(monthlyExpense.values());

            if (values.size() >= 3) {
                BigDecimal m1 = values.get(values.size() - 3);
                BigDecimal m2 = values.get(values.size() - 2);
                BigDecimal m3 = values.get(values.size() - 1);

                if (m3.compareTo(m2) > 0 && m2.compareTo(m1) > 0) {
                    insights.add("📈 Your expenses increased continuously for the last 3 months.");
                }
            }

            // 🟢 4) Total spending + good job message
            BigDecimal total = categoryTotals.values()
                    .stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            insights.add("💰 Total spending this month: ₹" + total);

            if (total.compareTo(BigDecimal.valueOf(20000)) < 0) {
                insights.add("✅ Good job! Your overall spending is under control.");
            } else {
                insights.add("⚠ You spent above ₹20,000 this month. Consider setting a budget.");
            }

            // 🔮 5) Forecast next month spending (simple AI style)
            if (values.size() >= 2) {
                BigDecimal last = values.get(values.size() - 1);
                BigDecimal prev = values.get(values.size() - 2);

                if (last.compareTo(prev) > 0) {
                    insights.add("🔮 If this trend continues, you may spend *more than ₹" +
                            last.add(BigDecimal.valueOf(2000)) + "* next month.");
                }
            }

            return insights;
        }

    }	
