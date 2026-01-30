package com.ExpenseTracker.mod;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    
    // Define the possible roles as an Enum
    public enum RoleName {
        
    	ROLE_ADMIN , ROLE_USER;

    	

    }
    private Transaction.Type type;
    public Transaction.Type getType() {
		return type;
	}

	public void setType(Transaction.Type type) {
		this.type = type;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RoleName name;

	
	public Role(Integer id, RoleName name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public RoleName getName() {
		return name;
	}

	public void setName(RoleName name) {
		this.name = name;
	}

	
	    // 1. **REQUIRED BY JPA**: Default (no-arg) constructor
	    public Role() {
	    }

	    // 2. **REQUIRED FOR FIXING YOUR ERROR**: Constructor that takes RoleName
	    public Role(RoleName name) { // <-- This is the missing piece
	        this.name = name;
	    }

	    // Getters and Setters...

	 
	
    // Getters, Setters, Constructors...
    // Note: User entities will reference this in a ManyToMany relationship.
}