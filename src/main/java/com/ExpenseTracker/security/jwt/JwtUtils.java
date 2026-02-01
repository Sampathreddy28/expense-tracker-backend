package com.ExpenseTracker.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.ExpenseTracker.security.key.KeyGenerator;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
   
//    String jwtSecret = KeyGenerator.generateJwtToken();

//    @Value("${expensetracker.app.jwtSecret}")
//    private String jwtSecret;

//    @Value("${expensetracker.app.jwtExpirationMs}")
//    private int jwtExpirationMs;
  @Value("${expensetracker.app.jwt.secret}")
private String jwtSecret;

@Value("${expensetracker.app.jwt.expiration.ms}")
private long jwtExpirationMs;


    
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            System.out.println("JWT Secret length: " + jwtSecret.length());

    }

    // Generate token (unchanged - no deprecation here)
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        String authorities = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("roles", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    // Modern, non-deprecated way to extract username
    public String getUsernameFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)  // ← Use setSigningKey instead of verifyWith
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // Modern, non-deprecated way to validate token
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)  // ← This is the correct modern replacement
                .build()
                .parseClaimsJws(token);  // throws exception if invalid
            return true;
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Optional: Extract roles
    public String getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("roles", String.class);
    }
}
