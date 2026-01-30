package com.ExpenseTracker.mod;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class RevokedToken {

    @Id
    private String token;

    private Instant revokedAt;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Instant getRevokedAt() {
		return revokedAt;
	}

	public void setRevokedAt(Instant revokedAt) {
		this.revokedAt = revokedAt;
	}
    
}
