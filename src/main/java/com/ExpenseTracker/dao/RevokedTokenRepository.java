package com.ExpenseTracker.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ExpenseTracker.mod.RevokedToken;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {}
