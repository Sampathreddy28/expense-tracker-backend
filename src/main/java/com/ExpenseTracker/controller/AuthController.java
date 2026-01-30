package com.ExpenseTracker.controller;

import com.ExpenseTracker.dao.RoleRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.dao.request.LoginRequest;
import com.ExpenseTracker.dao.request.SignupRequest;
import com.ExpenseTracker.mod.Role;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.JwtResponse;
import com.ExpenseTracker.mod.dto.response.MessageResponse;
import com.ExpenseTracker.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create user and assign ROLE_USER
        User user = new User(request.getUsername(),
                             request.getEmail(),
                             passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));
    }
}
