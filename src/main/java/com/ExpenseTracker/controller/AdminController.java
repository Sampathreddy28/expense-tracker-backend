package com.ExpenseTracker.controller;


import com.ExpenseTracker.dao.AdminService;
import com.ExpenseTracker.dao.RoleRepository;
import com.ExpenseTracker.dao.TokenRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.dao.request.LoginRequest;
import com.ExpenseTracker.dao.request.SignupRequest;
import com.ExpenseTracker.mod.CustomUserDetails;
import com.ExpenseTracker.mod.Role;
import com.ExpenseTracker.mod.User;
import com.ExpenseTracker.mod.dto.CategoryAnalyticsDto;
import com.ExpenseTracker.mod.dto.response.AdminChartResponse;
import com.ExpenseTracker.mod.dto.response.AdminMetricsResponse;
import com.ExpenseTracker.security.jwt.JwtUtils;
import com.ExpenseTracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final UserService userService;
	private final AdminService adminService;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	 private final AuthenticationManager authenticationManager;
	 
	 private final TokenRepository tokenRepository;
	 private final JwtUtils jwtUtils;
	@Autowired
	public AdminController(UserService userService, AdminService adminService, UserRepository userRepository,
			RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager, TokenRepository tokenRepository) {
		this.userService = userService;
		this.adminService = adminService;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.tokenRepository = tokenRepository;
		this.jwtUtils = jwtUtils;
	}

	    // ✅ POST ADMIN LOGIN
		    

	
	@GetMapping("/dashboard")
	public String dashboard() {
		return "ADMIN ACCESS GRANTED";
	}

	@GetMapping("/users")
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userService.findAllUsers());
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok("User deleted successfully.");
	}

	@PutMapping("/users/{id}/promote")
	public ResponseEntity<String> promoteToAdmin(@PathVariable Long id) {
		adminService.promoteUserToAdmin(id);
		return ResponseEntity.ok("User promoted to ADMIN");
	}

	@PutMapping("/users/{id}/block")
	public ResponseEntity<String> blockUser(@PathVariable Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
		user.setEnabled(false);
		userRepository.save(user);
		return ResponseEntity.ok("User blocked successfully");
	}
	@PutMapping("/users/{id}/unblock")
	public ResponseEntity<Map<String, String>> unblockUser(@PathVariable Long id) {

	    User user = userRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    user.setEnabled(true);
	    userRepository.save(user);

	    return ResponseEntity.ok(
	        Map.of("message", "User unblocked successfully")
	    );
	}
	
	 @PostMapping("/users/{id}/force-logout")
	    public ResponseEntity<String> forceLogoutUser(@PathVariable Long id) {

	        User user = userRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	        tokenRepository.revokeAllTokensForUser(user.getUsername());

	        return ResponseEntity.ok("User logged out forcefully");
	    }


	@PostMapping("/create-admin")
	public ResponseEntity<?> createAdmin(@RequestBody SignupRequest req) {
		if (userRepository.existsByUsername(req.getUsername())) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username already exists"));
		}

		Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
				.orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

		User admin = new User();
		admin.setUsername(req.getUsername());
		admin.setEmail(req.getEmail());
		admin.setPassword(passwordEncoder.encode(req.getPassword()));
		admin.setRoles(Set.of(adminRole));

		userRepository.save(admin);

		return ResponseEntity.ok(Map.of("success", true, "message", "Admin created successfully", "data", admin));
	}
	
	@GetMapping("/metrics")
	public ResponseEntity<AdminMetricsResponse> getAdminMetrics() {
	    return ResponseEntity.ok(adminService.getMetrics());
	}
	@GetMapping("/analytics")
	public ResponseEntity<List<AdminChartResponse>> getAnalytics() {
	    return ResponseEntity.ok(adminService.getMonthlyAnalytics());
	}
	@GetMapping("/analytics/categories")
	public ResponseEntity<List<CategoryAnalyticsDto>> categoryAnalytics() {
	    return ResponseEntity.ok(adminService.getCategoryAnalytics());
	}

}
