package com.ExpenseTracker.mod;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Needed for roles
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User implements UserDetails { // 🔥 1. IMPLEMENT UserDetails HERE

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Category> categories;

    @ManyToMany(fetch = FetchType.EAGER) // 🔥 Changed to EAGER fetch for roles, as they are needed for authorization checks
    @JoinTable(name = "user_roles", 
               joinColumns = @JoinColumn(name = "user_id"), 
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    
    @Column(nullable = false)
    private boolean enabled = true;

    
    @Column(length = 15)
    private String phoneNumber;

    @Column
    private String telegramChatId;
    private boolean mobileAlertsEnabled = false;
    private boolean telegramAlertsEnabled = false;

    private boolean mobileReportsEnabled = false;
    private boolean telegramReportsEnabled = false;
  
    public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getTelegramChatId() {
		return telegramChatId;
	}

	public void setTelegramChatId(String telegramChatId) {
		this.telegramChatId = telegramChatId;
	}

	public boolean isAlertsEnabled() {
		return alertsEnabled;
	}

	public void setAlertsEnabled(boolean alertsEnabled) {
		this.alertsEnabled = alertsEnabled;
	}

	private boolean alertsEnabled = true;

    // --- CONSTRUCTORS (Kept your existing ones) ---
    public User() {
        super();
    }
    public boolean isMobileReportsEnabled() {
        return mobileReportsEnabled;
    }

    public void setMobileReportsEnabled(boolean mobileReportsEnabled) {
        this.mobileReportsEnabled = mobileReportsEnabled;
    }

    public boolean isTelegramReportsEnabled() {
        return telegramReportsEnabled;
    }

    public void setTelegramReportsEnabled(boolean telegramReportsEnabled) {
        this.telegramReportsEnabled = telegramReportsEnabled;
    }

    // Constructor used when retrieving roles from the database
    public User(Long id, String username, String email, String password, List<Transaction> transactions,
			List<Category> categories, Set<Role> roles) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.transactions = transactions;
		this.categories = categories;
		this.roles = roles;
	}
    
    public User(Long id, String username, String email, String password, List<Transaction> transactions,
			List<Category> categories, Set<Role> roles, boolean enabled, String phoneNumber, String telegramChatId,
			boolean alertsEnabled) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.transactions = transactions;
		this.categories = categories;
		this.roles = roles;
		this.enabled = enabled;
		this.phoneNumber = phoneNumber;
		this.telegramChatId = telegramChatId;
		this.alertsEnabled = alertsEnabled;
	}

	// Constructor used for registration
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // --- GETTERS AND SETTERS (Kept your existing ones) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // Note: getUsername() and getPassword() are now required for UserDetails
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }
    public void setPassword(String password) { this.password = password; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    // --- 2. REQUIRED UserDetails METHODS ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 🔥 Maps the JPA roles to Spring Security's GrantedAuthority objects
        return this.roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .collect(Collectors.toList());
    }
    public User(Long id) {
        this.id = id;
    }
    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    // These should return true unless you implement logic for locking/expiring accounts.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	  public boolean isMobileAlertsEnabled() {
	        return mobileAlertsEnabled;
	    }

	    public void setMobileAlertsEnabled(boolean mobileAlertsEnabled) {
	        this.mobileAlertsEnabled = mobileAlertsEnabled;
	    }

	    public boolean isTelegramAlertsEnabled() {
	        return telegramAlertsEnabled;
	    }

	    public void setTelegramAlertsEnabled(boolean telegramAlertsEnabled) {
	        this.telegramAlertsEnabled = telegramAlertsEnabled;
	    }

	   

	
}