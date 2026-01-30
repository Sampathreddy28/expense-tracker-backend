package com.ExpenseTracker.security.jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private List<String> roles;

    public JwtResponse(String token, Long id, String username,
                       Collection<? extends GrantedAuthority> authorities) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }
}
