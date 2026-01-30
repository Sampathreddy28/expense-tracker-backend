package com.ExpenseTracker.security;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ExpenseTracker.dao.RoleRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.mod.Role;
import com.ExpenseTracker.mod.User;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (userRepository.findByUsername("superadmin").isPresent()) {
                return; // admin already exists
            }

            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

            User admin = new User();
            admin.setUsername("superadmin");
            admin.setEmail("admin@system.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEnabled(true); // ✅ IMPORTANT
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);



            System.out.println("✅ Default ADMIN created: superadmin / Admin@123");
        };
    }
}
