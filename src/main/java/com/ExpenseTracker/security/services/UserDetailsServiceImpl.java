package com.ExpenseTracker.security.services;

import com.ExpenseTracker.dao.UserRepository;
import org.springframework.security.core.userdetails.User; // <--- This is the key import!

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	


	private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	com.ExpenseTracker.mod.User jpaUser = userRepository.findByUsername(username)
    	        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

    	    // 2. Build and return the Spring Security UserDetails object.
    	    // The .build() method returns org.springframework.security.core.userdetails.User, 
    	    // which implements UserDetails, matching the required return type.
    	    return User.builder()
    	            .username(jpaUser.getUsername())
    	            .password(jpaUser.getPassword())
    	            .authorities(jpaUser.getRoles().stream()
    	                    .map(role -> new SimpleGrantedAuthority(role.getName().name()))
    	                    .collect(Collectors.toList()))
    	            .build();
    }
}
