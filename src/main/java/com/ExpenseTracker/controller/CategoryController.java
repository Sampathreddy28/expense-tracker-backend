package com.ExpenseTracker.controller;

import com.ExpenseTracker.dao.CategoryRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.Category;
import com.ExpenseTracker.mod.Transaction;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategoryRequest;
import com.ExpenseTracker.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // Only one constructor that injects all dependencies
    public CategoryController(CategoryService categoryService, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // Helper method to get the current authenticated User
    private User getCurrentUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        // Use the injected repository instance
        
        	return userRepository.findByUsername(username)
        	        .orElseThrow(() -> new RuntimeException("User not found"));

		
    }


    // Endpoint 1: Create Category
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CategoryRequest request) {
        User currentUser = getCurrentUser();
        Category newCategory = categoryService.createCategory(currentUser.getId(), request.getName());
        return ResponseEntity.ok(newCategory);
    }

   

    // Endpoint 3: Delete Category
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        boolean success = categoryService.deleteCategory(currentUser.getId(), id);

        if (success) {
            return ResponseEntity.ok("Category deleted successfully.");
        } else {
            return ResponseEntity.status(403).body("Access Denied: You can only delete your own categories.");
        }
    }
    

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Category>> getAllCategories(
            @RequestParam(required = false) Transaction.Type type
    ) {
        User currentUser = getCurrentUser();

        List<Category> categories;

        if (type == null) {
            categories = categoryRepository.findByUserId(currentUser.getId());
        } else {
            categories = categoryRepository.findByUserIdAndType(
                    currentUser.getId(),
                    type
            );
        }

        return ResponseEntity.ok(categories);
    }

}
