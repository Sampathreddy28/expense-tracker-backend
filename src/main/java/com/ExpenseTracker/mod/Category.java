package com.ExpenseTracker.mod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Transaction.Type type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    
    
    // ✅ REQUIRED by JPA
    public Category() {}

    // ✅ THIS CONSTRUCTOR FIXES YOUR ERROR
    public Category(String name, Transaction.Type type, User user) {
        this.name = name;
        this.type = type;
        this.user = user;
    }

    public Category(Long id) {
        this.id = id;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Transaction.Type getType() { return type; }
    public User getUser() { return user; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(Transaction.Type type) { this.type = type; }
    public void setUser(User user) { this.user = user; }
}
