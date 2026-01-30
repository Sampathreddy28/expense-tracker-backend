package com.ExpenseTracker.security;

import com.ExpenseTracker.dao.RoleRepository;
import com.ExpenseTracker.mod.Role;
import com.ExpenseTracker.security.jwt.AuthEntryPointJwt;
import com.ExpenseTracker.security.jwt.AuthTokenFilter;
import com.ExpenseTracker.security.jwt.JwtUtils;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Final fields for dependency injection
    private final UserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final AuthEntryPointJwt unauthorizedHandler;

    // 2. Constructor Injection (No @Autowired needed here)
    public SecurityConfig(UserDetailsService userDetailsService, 
                          JwtUtils jwtUtils,
                          AuthEntryPointJwt unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    // 3. Define the JWT Filter Bean
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        // Correctly use the injected final fields to construct the filter
        return new AuthTokenFilter(jwtUtils, userDetailsService); 
    }

    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception ->
                exception.authenticationEntryPoint(unauthorizedHandler)
            )
//            .authorizeHttpRequests(auth -> auth
//
//                // 🔥 Allow browser preflight
//                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                .requestMatchers(
//                	    "/api/auth/**",
//                	    "/api/admin/**"
//                	).permitAll()
//                // ✅ Public
//                .requestMatchers(
//                    "/api/auth/**"
//                ).permitAll()
//
//                // 🔐 Admin-only
//                .requestMatchers("/api/admin/**").hasRole("ADMIN")
//
//                // 👤 User + Admin
//                .requestMatchers(
//                    "/api/user/**",
//                    "/api/transactions/**",
//                    "/api/budgets/**",
//                    "/api/categories/**",
//                    "/api/reports/**",
//                    "/api/analytics/**"
//                ).hasAnyRole("USER", "ADMIN")
//
//                // 🔒 Everything else
//                .anyRequest().authenticated()
//            )
            .authorizeHttpRequests(auth -> auth
            	    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            	    .requestMatchers("/api/auth/**").permitAll()
            	    .requestMatchers("/api/admin/**").hasRole("ADMIN")
            	    .requestMatchers(
            	        "/api/user/**",
            	        "/api/transactions/**",
            	        "/api/budgets/**",
            	        "/api/categories/**",
            	        "/api/reports/**",
            	        "/api/analytics/**",
            	        "/api/alerts/**"
            	    ).hasAnyRole("USER", "ADMIN")
            	    .anyRequest().authenticated()
            	)

            // 🔥 THIS WAS MISSING
            .addFilterBefore(
                authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    

    
   
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Your UserDetailsService must correctly load the user by username
        authProvider.setUserDetailsService(userDetailsService); 
        // This must match the encoder used when the password was saved
        authProvider.setPasswordEncoder(passwordEncoder()); 
        return authProvider;
    }
    
    // 7. Authentication Manager (used in AuthController for login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 8. Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            // Check if the role already exists to prevent duplicates
            if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
                Role userRole = new Role(Role.RoleName.ROLE_USER);
                roleRepository.save(userRole);
                System.out.println("✅ Initialized role: USER");
            }
            // You can add other roles here as well, e.g., ADMIN
            if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
                Role adminRole = new Role(Role.RoleName.ROLE_ADMIN);
                roleRepository.save(adminRole);
                System.out.println("✅ Initialized role: ADMIN");
            }
        };
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "https://your-frontend.vercel.app"
        ));
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
