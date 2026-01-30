package com.ExpenseTracker.dao.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class TransactionRequest {

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    private String description;

    @NotNull
    private String type; // EXPENSE or INCOME

    @NotNull
    private LocalDate date;

    @NotNull
    private Long categoryId;

    // ----- getters & setters -----

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
