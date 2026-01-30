package com.ExpenseTracker.security.key;
//Temporary code snippet to generate the password you need to save to MySQL
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
 public static void main(String[] args) {
     BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
     String rawPassword = "123456"; // Example: "password123"
     String encodedPassword = encoder.encode(rawPassword);
     System.out.println("Copy this encoded password to your MySQL user table:");
     System.out.println(encodedPassword);
 }
}