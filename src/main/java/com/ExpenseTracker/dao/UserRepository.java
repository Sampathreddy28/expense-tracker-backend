package com.ExpenseTracker.dao;

import com.ExpenseTracker.mod.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA automatically implements this method based on the name
    
	Optional<User> findByUsername(String username);
   // Checks if a user with a given email already exists (for registration validation)
    Boolean existsByEmail(String email);
    
    // Checks if a user with a given username already exists
    Boolean existsByUsername(String username);
    
    
    long countByEnabledTrue();

}