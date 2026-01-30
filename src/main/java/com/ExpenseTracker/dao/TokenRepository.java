package com.ExpenseTracker.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ExpenseTracker.mod.Token;

import jakarta.transaction.Transactional;

public interface TokenRepository extends JpaRepository<Token, Long> {

	Optional<Token> findByToken(String token);

    @Modifying
    @Query("""
        UPDATE Token t
        SET t.revoked = true, t.expired = true
        WHERE t.user.username = :username
    """)
    void revokeAllValidTokensByUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Token t
        SET t.revoked = true, t.expired = true
        WHERE t.user.username = :username
    """)
    void revokeAllTokensForUser(@Param("username") String username);
}