package com.ExpenseTracker.mod.dto.request; // Ensure this package matches your project structure

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.ExpenseTracker.mod.Transaction.Type;

/**
 * DTO for receiving transaction data from the client to create or update a transaction.
 */
public class TransactionRequest {

    /**
     * The amount of the transaction. Must be a positive value.
     */
    @NotNull(message = "Transaction amount is required.")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero.")
    private BigDecimal amount;

    /**
     * The date the transaction occurred. Required.
     */
    @NotNull(message = "Transaction date is required.")
    private LocalDate date;

    /**
     * An optional description of the transaction.
     */
    @Size(max = 255, message = "Description cannot exceed 255 characters.")
    private String description;
    @NotBlank(message = "Transaction type is required.")
    // Validates that the provided string is one of the valid types
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Transaction type must be INCOME or EXPENSE.")
    private String type; // <--- The field that generates getType()
    /**
     * ID of the associated Category. This must be a valid ID fetched from the database
     * in the service layer to link the Transaction to the Category entity.
     */
    @NotNull(message = "Category ID is required.")
    private Long categoryId;

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public TransactionRequest(
			@NotNull(message = "Transaction amount is required.") @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero.") BigDecimal amount,
			@NotNull(message = "Transaction date is required.") LocalDate date,
			@Size(max = 255, message = "Description cannot exceed 255 characters.") String description,
			@NotBlank(message = "Transaction type is required.") @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Transaction type must be INCOME or EXPENSE.") String type,
			@NotNull(message = "Category ID is required.") Long categoryId) {
		super();
		this.amount = amount;
		this.date = date;
		this.description = description;
		this.type = type;
		this.categoryId = categoryId;
	}

	public TransactionRequest() {
		super();
	}

	
    
    // NOTE: The User ID is often pulled from the JWT token (security context), 
    // but if the client must explicitly provide it, you would add:
    /*
    @NotNull(message = "User ID is required.")
    private Long userId;
    */
    
}