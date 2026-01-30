package com.ExpenseTracker.mod;

import java.math.BigDecimal;

import java.time.LocalDate;

import com.ExpenseTracker.exception.InvalidInputException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Model class representing a single financial transaction.
 * This class serves as the blueprint for the 'transactions' table in the database.
 */
@Entity
@Table(name = "transactions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user", "category"})
public class Transaction {
	

	    public enum Type {
	        INCOME, EXPENSE
	    }

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(nullable = false)
	    private String description;

	    @Column(nullable = false)
	    private BigDecimal amount;

	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private Type type;

	    private LocalDate date = LocalDate.now();

	    // Many-to-One relationship with User (who owns this transaction)
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;

	    // Many-to-One relationship with Category (optional)
	    @JsonIgnoreProperties({"user"})
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "category_id")
	    private Category category;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public LocalDate getDate() {
			return date;
		}

		public void setDate(LocalDate date) {
			this.date = date;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public Category getCategory() {
			return category;
		}

		public void setCategory(Category category) {
			this.category = category;
		}

		public Transaction(Long id, String description, BigDecimal amount, Type type, LocalDate date, User user,
				Category category) {
			super();
			this.id = id;
			this.description = description;
			this.amount = amount;
			this.type = type;
			this.date = date;
			this.user = user;
			this.category = category;
		}

		public Transaction() {
			super();
		}

		public Transaction(Long id, String description, BigDecimal amount, Type type, LocalDate date, User user) {
			super();
			this.id = id;
			this.description = description;
			this.amount = amount;
			this.type = type;
			this.date = date;
			this.user = user;
		}

		public Transaction(Long id, String description, BigDecimal amount, Type type, LocalDate date) {
			super();
			this.id = id;
			this.description = description;
			this.amount = amount;
			this.type = type;
			this.date = date;
		}

		public Transaction(Long id, String description, BigDecimal amount, Type type) {
			super();
			this.id = id;
			this.description = description;
			this.amount = amount;
			this.type = type;
		}

		public Transaction(Long id, String description, BigDecimal amount) {
			super();
			this.id = id;
			this.description = description;
			this.amount = amount;
		}

		public Transaction(BigDecimal amount, 
                Type type, 
                Category category, 
                String description, 
                LocalDate date,
                User user) {
 this.amount = amount;
 this.type = type;
 this.category = category;
 this.description = description;
 this.date = date;
 this.user = user; // Assuming you also link a User
}

		public Transaction(Double amount, 
                String type, 
                String category, 
                String description, 
                LocalDate date) {
 
 // 1. Convert Double amount to BigDecimal (Crucial for finance)
 if (amount == null) {
     throw new InvalidInputException("Amount cannot be null.");
 }
 this.amount = BigDecimal.valueOf(amount); 

 // 2. Convert String type to Enum (Requires the Transaction.Type Enum to be defined)
 try {
     this.type = Type.valueOf(type.toUpperCase());
 } catch (IllegalArgumentException e) {
     throw new InvalidInputException("Invalid transaction type: " + type);
 }
 
 // 3. **MAJOR PROBLEM HERE:** You cannot convert a String to a Category Entity.
 // For now, we set the Category to null, but this will break JPA saving later!
 // The calling code MUST be updated to fetch the Category Entity first.
 System.err.println("Warning: Constructor cannot map String category. Set to null.");
 this.category = null; // <-- THIS IS THE WEAK POINT
 
 this.description = description;
 this.date = date;
 
}

}


   
