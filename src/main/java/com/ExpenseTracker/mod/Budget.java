package com.ExpenseTracker.mod;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id") // Budget can be tied to a specific category
    private Category category; 

    @Column(nullable = false)
    private BigDecimal limitAmount;

    @Column(nullable = false)
    private YearMonth period; // Represents the year and month this budget applies to

    @Column(nullable = false)
    private BigDecimal spentAmount = BigDecimal.ZERO;

	public BigDecimal getSpentAmount() {
		return spentAmount;
	}

	public void setSpentAmount(BigDecimal spentAmount) {
		this.spentAmount = spentAmount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public BigDecimal getLimitAmount() {
		return limitAmount;
	}

	public void setLimitAmount(BigDecimal limitAmount) {
		this.limitAmount = limitAmount;
	}

	public YearMonth getPeriod() {
		return period;
	}

	public void setPeriod(YearMonth period) {
		this.period = period;
	}

	public Budget(Long id, User user, Category category, BigDecimal limitAmount, YearMonth period) {
		super();
		this.id = id;
		this.user = user;
		this.category = category;
		this.limitAmount = limitAmount;
		this.period = period;
	}

	public Budget() {
		super();
	}

	

	

 
}