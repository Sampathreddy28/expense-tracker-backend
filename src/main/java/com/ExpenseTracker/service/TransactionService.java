package com.ExpenseTracker.service;

import com.ExpenseTracker.dao.TransactionRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.dao.CategoryRepository;
import com.ExpenseTracker.exception.InvalidInputException;
import com.ExpenseTracker.exception.ResourceNotFoundException;
import com.ExpenseTracker.mod.AdminTransactionSummary;
import com.ExpenseTracker.mod.Category;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.User;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class TransactionService {
	private static final Logger log =
            LoggerFactory.getLogger(TransactionService.class);
	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final  BudgetService budgetService;
	public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository,
			CategoryRepository categoryRepository, BudgetService budgetService) {
		this.transactionRepository = transactionRepository;
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
		this.budgetService = budgetService;
	}


	/* ================= TRANSACTION HISTORY ================= */
	public List<Transaction> getTransactionHistory(Long userId) {
		return transactionRepository.findByUserIdOrderByDateDesc(userId);
	}

	/* ================= BALANCE ================= */
	public BigDecimal calculateBalance(Long userId) {

		BigDecimal income = transactionRepository.sumAmountByUserIdAndType(userId, Transaction.Type.INCOME);

		BigDecimal expense = transactionRepository.sumAmountByUserIdAndType(userId, Transaction.Type.EXPENSE);

		if (income == null)
			income = BigDecimal.ZERO;
		if (expense == null)
			expense = BigDecimal.ZERO;

		return income.subtract(expense);
	}

	@Transactional
	public void deleteTransaction(Long userId, Long transactionId) {

		Transaction transaction = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

		// 🔐 Security check
		if (!transaction.getUser().getId().equals(userId)) {
			throw new InvalidInputException("Unauthorized delete attempt");
		}

		transactionRepository.delete(transaction);
	}

	public List<Object[]> getMonthlySummary(Long userId) {
		return transactionRepository.monthlyExpenseSummary(userId);
	}

	public List<Object[]> getCategorySummary(Long userId) {
		return transactionRepository.getCategorySummary(userId);
	}

	public List<Object[]> getMonthlyExpenseSummary(Long userId) {
		return transactionRepository.getMonthlyExpenseSummary(userId);
	}

	@Transactional
	public Transaction updateTransaction(Long userId, Long transactionId, Transaction updatedTransaction,
			Long categoryId) {

		Transaction existing = transactionRepository.findById(transactionId)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

		// 🔐 Security: ensure user owns the transaction
		if (!existing.getUser().getId().equals(userId)) {
			throw new InvalidInputException("Unauthorized update attempt");
		}

		Category category = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		if (updatedTransaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidInputException("Amount must be positive");
		}

		existing.setAmount(updatedTransaction.getAmount());
		existing.setDescription(updatedTransaction.getDescription());
		existing.setType(updatedTransaction.getType());
		existing.setDate(updatedTransaction.getDate());
		existing.setCategory(category);
		System.out.println("Update payload: " + updatedTransaction);
		System.out.println("CategoryId: " + categoryId);

		return transactionRepository.save(existing);
	}

	public Page<Transaction> getUserTransactions(Long userId, String search, Transaction.Type type, Long categoryId,
			LocalDate startDate, LocalDate endDate, Pageable pageable) {

		if (search == null)
			search = "";
		if (startDate == null)
			startDate = LocalDate.of(2000, 1, 1);
		if (endDate == null)
			endDate = LocalDate.now();

		return transactionRepository.findFilteredTransactions(userId, search.toLowerCase(), type, categoryId, startDate,
				endDate, pageable);
	}

	public Page<Transaction> getAdminTransactions(int page, int size, String search, String type, String username,
	        LocalDate startDate, LocalDate endDate, String sortBy, String direction) {

	    Sort sort = Sort.by(direction.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
	    Pageable pageable = PageRequest.of(page, size, sort);

	    Specification<Transaction> spec = (root, query, cb) -> {
	        List<Predicate> predicates = new ArrayList<>();

	        if (search != null && !search.isEmpty()) {
	            predicates.add(cb.like(cb.lower(root.get("description")), "%" + search.toLowerCase() + "%"));
	        }

	        if (type != null && !type.isEmpty()) {
	            predicates.add(cb.equal(root.get("type"), type));
	        }

	        if (username != null && !username.isEmpty()) {
	            predicates.add(cb.equal(root.join("user").get("username"), username));
	        }

	        if (startDate != null && endDate != null) {
	            predicates.add(cb.between(root.get("date"), startDate, endDate));
	        }

	        return cb.and(predicates.toArray(new Predicate[0]));
	    };

	    return transactionRepository.findAll(spec, pageable);
	}

	 public AdminTransactionSummary getTotals() {
	        Double income = transactionRepository.sumByType(Transaction.Type.INCOME);
	        Double expense = transactionRepository.sumByType(Transaction.Type.EXPENSE);
	        return new AdminTransactionSummary(
	                income == null ? 0 : income,
	                expense == null ? 0 : expense
	        );
	    }
	 @Transactional
	 public Transaction createTransaction(
	         Long userId,
	         Transaction transaction,
	         Long categoryId
	 ) {
	     // 1️⃣ Validate amount (already correct)
	     if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
	         throw new IllegalArgumentException("Transaction amount must be positive");
	     }

	     // 2️⃣ Set user & category
	     User user = userRepository.findById(userId)
	             .orElseThrow(() -> new RuntimeException("User not found"));

	     transaction.setUser(user);

	     if (categoryId != null) {
	         Category category = categoryRepository.findById(categoryId)
	                 .orElseThrow(() -> new RuntimeException("Category not found"));
	         transaction.setCategory(category);
	     }

	     // 3️⃣ Save transaction
	     Transaction saved = transactionRepository.save(transaction);

	     
		 // 4️⃣ Update budget spent amount
	     budgetService.updateSpentAmount(
	             userId,
	             categoryId,
	             YearMonth.from(saved.getDate()),
	             saved.getAmount()
	     );

	     // 5️⃣ 🔔 Trigger budget alert automatically
	     budgetService.checkBudgetAfterTransaction(
	             userId,
	             categoryId,
	             YearMonth.from(saved.getDate())
	     ).ifPresent(alert -> {
	         // for now → log (later email / notification)
	         log.warn("BUDGET ALERT for user {}: {}", userId, alert);
	     });

	     return saved;
	 }

	 
}
