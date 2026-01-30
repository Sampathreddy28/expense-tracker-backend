package com.ExpenseTracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus ensures Spring converts this exception into a 404 HTTP response
@ResponseStatus(HttpStatus.NOT_FOUND) 
public class ResourceNotFoundException extends RuntimeException {

    // Standard constructor that takes a message
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Optional: Standard constructor that takes a message and a cause
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}