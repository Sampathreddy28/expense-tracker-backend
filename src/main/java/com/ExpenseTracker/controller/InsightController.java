package com.ExpenseTracker.controller;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.service.SpendingInsightService;
@RestController
@RequestMapping("/api/insights")
public class InsightController {

    private final SpendingInsightService insightService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    
    public InsightController(SpendingInsightService insightService, TransactionRepository transactionRepository,
			UserRepository userRepository) {
		super();
		this.insightService = insightService;
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
	}


    @GetMapping("/monthly")
    public List<String> getMonthlyInsights(Authentication auth) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow();

        Map<YearMonth, BigDecimal> monthly =
                transactionRepository.findMonthlyExpense(user.getId());

        List<Object[]> rawCategories =
                transactionRepository.findCategoryTotals(user.getId());

        Map<String, BigDecimal> categories =
                rawCategories.stream()
                        .collect(Collectors.toMap(
                                row -> (String) row[0],
                                row -> (BigDecimal) row[1]
                        ));

        return insightService.generateInsights(monthly, categories);
    }


}
