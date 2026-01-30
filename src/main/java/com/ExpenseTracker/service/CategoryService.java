package com.ExpenseTracker.service;

import com.ExpenseTracker.dao.CategoryRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.Category;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new expense category for the specified user.
     */
    public Category createCategory(Long userId, String categoryName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Category category = new Category();
        category.setName(categoryName);
        category.setUser(user);

        return categoryRepository.save(category);
    }

    /**
     * Retrieves all categories created by the specified user.
     */
    public List<Category> getUserCategories(Long userId) {
        // Uses the custom method defined in CategoryRepository
        return categoryRepository.findByUserId(userId);
    }

    /**
     * Deletes a category only if it belongs to the specified user.
     */
    public boolean deleteCategory(Long userId, Long categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);

        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            // Security check: Ensure the category belongs to the requesting user
            if (!category.getUser().getId().equals(userId)) {
                throw new SecurityException("Category does not belong to the authenticated user.");
            }
            // Note: Deleting a category might require updating or deleting related Transactions first.
            categoryRepository.delete(category);
            return true;
        }
        return false;
    }
    public void createDefaultCategories(User user) {

        if (!categoryRepository.findByUserId(user.getId()).isEmpty()) return;

        List<Category> defaults = List.of(
            new Category("FOOD", Transaction.Type.EXPENSE, user),
            new Category("GROCERIES", Transaction.Type.EXPENSE, user),
            new Category("RENT", Transaction.Type.EXPENSE, user),
            new Category("UTILITIES", Transaction.Type.EXPENSE, user),
            new Category("TRANSPORTATION", Transaction.Type.EXPENSE, user),
            new Category("SALARY", Transaction.Type.INCOME, user)
        );

        categoryRepository.saveAll(defaults);
    }
    
    @Transactional
    public void fixMissingCategoryTypes(User user) {
        List<Category> categories = categoryRepository.findByUser(user);

        for (Category c : categories) {
            if (c.getType() == null) {
                c.setType(Transaction.Type.EXPENSE); // default
            }
        }
    }

}