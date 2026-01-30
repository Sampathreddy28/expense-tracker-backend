package com.ExpenseTracker.mod.dto; // Adjust package as needed

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for receiving category data from the client (e.g., creating a new category).
 */
 // Generates a constructor with all fields
public class CategoryRequest {

    /**
     * The name of the category (e.g., "Groceries", "Utilities", "Rent").
     * Must not be null or empty, and must meet size constraints.
     */
    @NotBlank(message = "Category name is required.")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters.")
    // Optional: Ensure it only contains letters, numbers, and spaces
    @Pattern(regexp = "^[a-zA-Z0-9 ]*$", message = "Category name must only contain letters, numbers, and spaces.")
    private String name;

    /**
     * An optional brief description of the category.
     */
    @Size(max = 255, message = "Description cannot exceed 255 characters.")
    private String description;

    /**
     * The type of the category: INCOME or EXPENSE.
     * Use a string for flexibility, or map to an Enum in the service layer.
     */
    @NotBlank(message = "Category type (INCOME/EXPENSE) is required.")
    // Optional: Validate that the type is one of the allowed values
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Category type must be INCOME or EXPENSE.")
    private String type;

	public CategoryRequest(
			@NotBlank(message = "Category name is required.") @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters.") @Pattern(regexp = "^[a-zA-Z0-9 ]*$", message = "Category name must only contain letters, numbers, and spaces.") String name,
			@Size(max = 255, message = "Description cannot exceed 255 characters.") String description,
			@NotBlank(message = "Category type (INCOME/EXPENSE) is required.") @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Category type must be INCOME or EXPENSE.") String type) {
		super();
		this.name = name;
		this.description = description;
		this.type = type;
	}

	public CategoryRequest() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
    
    
    
}