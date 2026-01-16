package com.social.bookshare.domain;

import com.social.bookshare.config.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_keys")
public class UserKey extends BaseEntity {
	
	protected UserKey() {}

    public UserKey(Builder builder) {
        this.user = builder.user;
        this.type = builder.type;
        this.key = builder.key;
    }

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String type; // GEMINI, OPENAI, ...

    @Column(nullable = false, length = 500)
    private String key;

    public void updateKey(String encryptedKey) {
        this.key = encryptedKey;
    }
    
    // Getters
    public String getKey() { return key; }
    public String getType() { return type; }
    
    // Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
    	private User user;
    	private String type;
    	private String key;
    	
    	public Builder user(User user) {
            this.user = user;
            return this;
        }
    	
    	public Builder type(String type) {
            this.type = type.strip().toUpperCase();
            return this;
        }
    	
    	public Builder key(String encryptedKey) {
            this.key = encryptedKey.strip();
            return this;
        }
    	
    	public UserKey build() {
    		return new UserKey(this);
    	}
    }
}
