package com.ExpenseTracker.dao;

import com.ExpenseTracker.mod.Category;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Finds all categories belonging to a specific user
    List<Category> findByUserId(Long userId);
    
    List<Category> findByUserIdAndType(Long userId, Transaction.Type type);

    boolean existsByUserId(Long userId);

    List<Category> findByUser(User user);
}