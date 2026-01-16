package com.social.bookshare.domain;

import java.util.Base64;

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
		this.encryptionSalt = builder.encryptionSalt;
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
    
    @Column(name = "encryption_salt", nullable = false, length = 64)
    private String encryptionSalt; // Base64
    
    public enum Role {
    	ADMIN, USER, GUEST
    }
    
    public void updatePassword(String password) { // Get encoded only
        if (password == null || password.isBlank()) {
        	throw new IllegalArgumentException("Password cannot be null");
        }
        this.password = password.strip();
    }
    
    // 2FA
    public void updateTfaSecret(String encryptedSecret) {
    	this.tfaSecret = encryptedSecret.strip();
    }
    
    public void updateIsTfaEnabled(boolean isTfaEnabled) {
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
	public byte[] getDecodedSalt() { return Base64.getDecoder().decode(this.encryptionSalt); }
	
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
        private String encryptionSalt;

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
            this.password = password.strip(); // Encoder will check
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }
        
        public Builder encryptionSalt(String encryptionSalt) {
        	this.encryptionSalt = encryptionSalt;
        	return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
