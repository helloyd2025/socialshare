package com.social.bookshare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	protected User() {}
	
	private User(Builder builder) {
		this.id = builder.id;
		this.email = builder.email;
		this.name = builder.name;
		this.password = builder.password;
		this.role = builder.role;
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true, nullable = false)
    private String name; // nickname

    @Column(nullable = false, length = 150)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;
    
    @Column(name = "tfa_secret")
    private String tfaSecret;
    
    @Column(name = "is_tfa_enabled", nullable = false)
    private boolean isTfaEnabled = false;
    
    public enum Role {
    	ADMIN, USER, GUEST
    }
    
    public void updateUserPassword(String password) { // Get encoded only
        if (password == null || password.isBlank()) 
            throw new IllegalArgumentException("Password cannot be null");

        this.password = password.strip();
    }
    
    // 2FA
    public void updateUserTfaSecret(String tfaSecret) {
    	this.tfaSecret = tfaSecret.strip();
    }
    
    public void updateUserIsTfaEnabled(boolean isTfaEnabled) {
    	this.isTfaEnabled = isTfaEnabled;
    }

    // Getters
    public Long getId() { return id; }
	public String getEmail() { return email; }
	public String getName() { return name; }
	public String getPassword() { return password; }
	public Role getRole() { return role; }
	public String getTfaSecret() { return tfaSecret; }
	public boolean isTfaEnabled() { return isTfaEnabled; }
	
	// Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private String email;
        private String name;
        private String password;
        private Role role;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email.strip().toLowerCase();
            return this;
        }
        
        public Builder name(String name) {
        	this.name = name.strip(); // No case restrictions
        	return this;
        }

        public Builder password(String password) {
            this.password = password.strip();
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
