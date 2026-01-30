package com.ExpenseTracker.mod.dto;

public class AlertMessage {
    private String title;
    private String message;

    public AlertMessage(String title, String message) {
        this.title = title;
        this.message = message;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
    
    
}
