package com.ExpenseTracker.service;

import com.ExpenseTracker.dao.RoleRepository;
import com.ExpenseTracker.dao.UserRepository;
import com.ExpenseTracker.dao.request.SignupRequest;
import com.ExpenseTracker.exception.ResourceNotFoundException;
import com.ExpenseTracker.mod.Role;
import com.ExpenseTracker.mod.Role.RoleName;
import com.ExpenseTracker.mod.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User registerUser(SignupRequest request) {

		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
				.orElseThrow(() -> new RuntimeException("Role USER not found"));

		user.setRoles(Set.of(userRole));
		return userRepository.save(user);
	}

	public List<User> findAllUsers() {
		return userRepository.findAll();
	}

	public void deleteUser(Long userId) {
		userRepository.deleteById(userId);
	}

	public Long getUserIdFromUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("User not found" + username)).getId();
	}

	public User getUserByUsername(String username) {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
	}

}
