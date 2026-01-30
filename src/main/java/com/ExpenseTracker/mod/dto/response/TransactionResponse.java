package com.ExpenseTracker.mod.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private String type;
    private LocalDate date;
    private Long categoryId;
    private String categoryName;
	public TransactionResponse(Long id, String description, BigDecimal amount, String type, LocalDate date,
			Long categoryId, String categoryName) {
		super();
		this.id = id;
		this.description = description;
		this.amount = amount;
		this.type = type;
		this.date = date;
		this.categoryId = categoryId;
		this.categoryName = categoryName;
	}
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
}
