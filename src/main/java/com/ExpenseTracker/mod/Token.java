package com.ExpenseTracker.mod;

import jakarta.persistence.*;

@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 500)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType = TokenType.BEARER;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private boolean expired;

    // 🔗 Many tokens → One user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
