package com.ExpenseTracker.controller;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

import com.ExpenseTracker.dao.TransactionServiceRepo;
import com.ExpenseTracker.mod.AdminTransactionSummary;
import com.ExpenseTracker.mod.CustomUserDetails;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.dto.request.TransactionRequest;
import com.ExpenseTracker.mod.dto.response.BalanceResponse;
import com.ExpenseTracker.security.services.TransactionServiceImpl;
import com.ExpenseTracker.service.TransactionService;
import com.ExpenseTracker.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService transactionService;
	private final UserService userService;
	private final TransactionServiceRepo transactionServiceRepo;
	private final TransactionServiceImpl transactionServiceImpl;

	public TransactionController(TransactionService transactionService, UserService userService,
			TransactionServiceRepo transactionServiceRepo, TransactionServiceImpl transactionServiceImpl) {
		this.transactionService = transactionService;
		this.userService = userService;
		this.transactionServiceRepo = transactionServiceRepo;
		this.transactionServiceImpl = transactionServiceImpl;
	}

	@PostMapping
	public ResponseEntity<Transaction> addTransaction(@AuthenticationPrincipal UserDetails userDetails,
			@Valid @RequestBody TransactionRequest request) {

		Long userId = userService.getUserIdFromUsername(userDetails.getUsername());

		Transaction transaction = new Transaction();
		transaction.setAmount(request.getAmount());
		transaction.setDescription(request.getDescription());
		transaction.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());

		try {
			transaction.setType(Transaction.Type.valueOf(request.getType().toUpperCase()));
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transaction type");
		}

		Transaction saved = transactionService.createTransaction(userId, transaction, request.getCategoryId());

		return ResponseEntity.ok(saved);
	}

	@GetMapping("/balance")
	public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal UserDetails userDetails) {

		Long userId = userService.getUserIdFromUsername(userDetails.getUsername());
		BigDecimal balance = transactionService.calculateBalance(userId);
		return ResponseEntity.ok(new BalanceResponse(balance));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTransaction(@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long id) {

		Long userId = userService.getUserIdFromUsername(userDetails.getUsername());
		transactionService.deleteTransaction(userId, id);

		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}")
	public ResponseEntity<Transaction> updateTransaction(@AuthenticationPrincipal UserDetails userDetails,
			@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {

		Long userId = userService.getUserIdFromUsername(userDetails.getUsername());

		Transaction transaction = new Transaction();
		transaction.setAmount(request.getAmount());
		transaction.setDescription(request.getDescription());
		transaction.setDate(request.getDate());
		transaction.setType(Transaction.Type.valueOf(request.getType().toUpperCase()));

		Transaction updated = transactionService.updateTransaction(userId, id, transaction, request.getCategoryId());

		return ResponseEntity.ok(updated);
	}

	@GetMapping("/summary/category")
	public ResponseEntity<List<Object[]>> getCategorySummary(@AuthenticationPrincipal UserDetails userDetails) {

		Long userId = userService.getUserIdFromUsername(userDetails.getUsername());
		return ResponseEntity.ok(transactionService.getCategorySummary(userId));
	}

	@GetMapping("/summary/monthly")
	public ResponseEntity<List<Object[]>> getMonthlySummary(@AuthenticationPrincipal UserDetails userDetails) {

		Long userId = userService.getUserIdFromUsername(userDetails.getUsername());
		return ResponseEntity.ok(transactionService.getMonthlyExpenseSummary(userId));
	}

	@GetMapping
	public Page<Transaction> getUserTransactions(@AuthenticationPrincipal CustomUserDetails user,

			@RequestParam(required = false) String search, @RequestParam(required = false) Transaction.Type type,
			@RequestParam(required = false) Long categoryId, @RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate,

			Pageable pageable) {
		return transactionService.getUserTransactions(user.getUser().getId(), search, type, categoryId, startDate,
				endDate, pageable);
	}

}
