package com.ExpenseTracker.dao;

import com.ExpenseTracker.mod.Role;
import com.ExpenseTracker.mod.Role.RoleName;
import com.ExpenseTracker.mod.User;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    
    // Find a role by its enumerated name
    Optional<Role> findByName(Role.RoleName name);

}