package com.ExpenseTracker.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ExpenseTracker.dao.TransactionDAO;
import com.ExpenseTracker.mod.Transaction;

public class ExpenseService {

    private final TransactionDAO transactionDAO;

    // Constructor to inject the DAO dependency
    public ExpenseService(TransactionDAO dao) {
        this.transactionDAO = dao;
        // Ensure the table exists when the service starts
        transactionDAO.createTable(); 
    }

    // --- CRUD METHODS ---

    /**
     * Retrieves a single transaction by its ID.
     */
    public Transaction getTransactionById(int id) {
        // Delegates to the DAO's method
        return transactionDAO.getTransactionById(id);
    }
    
    /**
     * Retrieves all transactions.
     */
    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }
    
    /**
     * Adds a new transaction.
     */
 // Inside your TransactionService
    public int addTransaction(Transaction transaction) {
        
        // Define a BigDecimal zero for comparison
        final BigDecimal ZERO = BigDecimal.ZERO; 
        
        // Business logic for input validation: Check if amount <= 0
        // The expression (compareTo(ZERO) <= 0) means "less than or equal to zero"
        if (transaction.getAmount().compareTo(ZERO) <= 0) { 
            
            // Throw the appropriate exception
            throw new IllegalArgumentException("Transaction amount must be positive."); 
            
        }
        
        // Note: Assuming you update this to use the injected repository (transactionRepository.save)
        return transactionDAO.addTransaction(transaction); // Still using old DAO reference here
    }

    /**
     * Updates an existing transaction.
     * @return true if the update was successful.
     */
    public boolean updateTransaction(Transaction transaction) {
        // Delegates to the DAO's method
        return transactionDAO.updateTransaction(transaction);
    }

    /**
     * Deletes a transaction by its ID.
     * @return true if the deletion was successful.
     */
    public boolean deleteTransaction(int id) {
        // Delegates to the DAO's method
        return transactionDAO.deleteTransaction(id);
    }

    // --- DATA ANALYSIS METHODS ---

    /**
     * Filters transactions based on category, type, and date range.
     * This is useful for building dynamic reports and visualizations.
     * @param category The category to filter by (or null for all).
     * @param type The type to filter by ("Income" or "Expense") (or null for both).
     * @param startDate The start date (inclusive) to filter by (or null for no start limit).
     * @param endDate The end date (inclusive) to filter by (or null for no end limit).
     * @return A list of filtered transactions.
     */
 // Inside your Service or Business Logic class

    public List<Transaction> filterTransactions(String category, String type, LocalDate startDate, LocalDate endDate) {
        
        // NOTE: This entire method is architecturally inefficient (see point 2 below)
        return transactionDAO.getAllTransactions().stream()
            // Filter by Category (FIX: Must access the name field of the Category entity)
            .filter(t -> category == null || t.getCategory().getName().equalsIgnoreCase(category))
            
            // Filter by Type (FIX: Must convert the String 'type' parameter to the Enum type for comparison)
            .filter(t -> type == null || t.getType() == Transaction.Type.valueOf(type.toUpperCase()))
            
            // Filter by Start Date (These date filters were already correct)
            .filter(t -> startDate == null || !t.getDate().isBefore(startDate))
            
            // Filter by End Date
            .filter(t -> endDate == null || !t.getDate().isAfter(endDate))
            
            .collect(Collectors.toList());
    }

    /**
     * Calculates the net balance (Total Income - Total Expense).
     * @return The overall balance.
     */
    
    
    
    
    // Assuming your Enum constants are defined like this:
    // public enum Type { INCOME, EXPENSE }

 // Inside your Service or DAO logic
    public BigDecimal calculateNetBalance() { 
        // Return type is changed to BigDecimal!
        
        // FIX 1: Change accumulator variables from 'double' to 'BigDecimal'
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        BigDecimal totalIncome = BigDecimal.ZERO; 
        BigDecimal totalExpense = BigDecimal.ZERO;
        
        for (Transaction t : transactions) {
            if (t.getType() == Transaction.Type.INCOME) { 
                // FIX 2: Use the add() method
                totalIncome = totalIncome.add(t.getAmount()); // ✅ CORRECT
            } 
            else if (t.getType() == Transaction.Type.EXPENSE) { 
                // FIX 2: Use the add() method
                totalExpense = totalExpense.add(t.getAmount()); // ✅ CORRECT
            }
        }
        
        // FIX 3: Use the subtract() method for the return value
        return totalIncome.subtract(totalExpense); // ✅ CORRECT
    }

    /**
     * Calculates the total amount spent for each expense category.
     * NOTE: The methods getExpenseSummaryByCategory1 and getExpenseSummaryByCategory are identical; only one is needed.
     * @return A Map where Key=Category Name (String), Value=Total Amount (Double).
     */
 // Inside your Service or Business Logic class

 // Inside your Service or Business Logic class
 // Note: Return types are now BigDecimal to maintain accuracy

 public Map<String, BigDecimal> getExpenseSummaryByCategory() {
     // FIX: Change return type value from Double to BigDecimal
     List<Transaction> transactions = transactionDAO.getAllTransactions(); 
     
     return transactions.stream()
         .filter(t -> t.getType() == Transaction.Type.EXPENSE)
         .collect(Collectors.groupingBy(
             t -> t.getCategory().getName(),
             // FIX: Use Collectors.reducing for BigDecimal summation
             Collectors.reducing(
                 BigDecimal.ZERO,           // Initial value
                 Transaction::getAmount,    // Map function (already returns BigDecimal)
                 BigDecimal::add            // Accumulator (BigDecimal.add)
             )
         ));
 }

 public Map<String, BigDecimal> getMonthlyExpenseTrend() {
     // FIX: Change return type value from Double to BigDecimal
     List<Transaction> transactions = transactionDAO.getAllTransactions();
     
     return transactions.stream()
         .filter(t -> t.getType() == Transaction.Type.EXPENSE)
         .collect(Collectors.groupingBy(
             t -> t.getDate().toString().substring(0, 7), 
             // FIX: Use Collectors.reducing for BigDecimal summation
             Collectors.reducing(
                 BigDecimal.ZERO, 
                 Transaction::getAmount, 
                 BigDecimal::add
             )
         ));
 }
 
}