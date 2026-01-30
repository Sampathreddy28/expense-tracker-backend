package com.ExpenseTracker.exception; // Your package for custom exceptions

/**
 * Custom exception to be thrown when user input data fails validation 
 * or required conditions are not met (e.g., invalid enum value, bad date format).
 * Extends RuntimeException so it does not need to be explicitly caught.
 */
public class InvalidInputException extends RuntimeException {

    // Standard constructor that accepts an error message
    public InvalidInputException(String message) {
        super(message);
    }

    // Optional: Constructor that accepts a message and a cause
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}