package com.ExpenseTracker.mod;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ExpenseTracker.dao.CategoryRepository;
import com.ExpenseTracker.dao.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryDataInitializer {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository, UserRepository userRepository) {
		this.categoryRepository = categoryRepository;
		this.userRepository = userRepository;
	}

		
	@EventListener(ApplicationReadyEvent.class)
	public void seedCategories() {

	    List<User> users = userRepository.findAll();

	    for (User user : users) {

	        if (categoryRepository.existsByUserId(user.getId())) {
	            continue;
	        }

	        List<Category> categories = List.of(
	            new Category("FOOD", Transaction.Type.EXPENSE, user),
	            new Category("GROCERIES", Transaction.Type.EXPENSE, user),
	            new Category("RENT", Transaction.Type.EXPENSE, user),
	            new Category("UTILITIES", Transaction.Type.EXPENSE, user),
	            new Category("TRANSPORTATION", Transaction.Type.EXPENSE, user),
	            new Category("SALARY", Transaction.Type.INCOME, user)
	        );

	        categoryRepository.saveAll(categories);
	    }
	}
	@PostConstruct
	public void fixNullTypes() {
	    List<Category> categories = categoryRepository.findAll();

	    for (Category c : categories) {
	        if (c.getType() == null) {
	            if (c.getName().equalsIgnoreCase("SALARY")) {
	                c.setType(Transaction.Type.INCOME);
	            } else {
	                c.setType(Transaction.Type.EXPENSE);
	            }
	        }
	    }

	    categoryRepository.saveAll(categories);
	}

}
