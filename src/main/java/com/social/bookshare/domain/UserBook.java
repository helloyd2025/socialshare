package com.social.bookshare.domain;

import java.sql.Timestamp;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_books")
public class UserBook {

	protected UserBook() {}
	
	public UserBook(Builder builder) {
		this.id = builder.id;
		this.user = builder.user;
		this.location = builder.location;
		this.book = builder.book;
		this.comment = builder.comment;
		this.status = builder.status;
		this.updatedAt = builder.updatedAt;
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id", referencedColumnName = "id", nullable = false)
	private Location location;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "isbn13", referencedColumnName = "id", nullable = false)
    private Book book;
	
	@Column(columnDefinition = "TEXT")
	private String comment;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private Timestamp updatedAt;
	
	public enum Status {
		AVAILABLE, RENTED, HIDDEN, EXPIRED, BANNED
	}
	
	public void updateUserBook(Location location, String comment, Status status) {
		this.location = location;
		this.comment = comment;
		this.status = status;
	}

	// Getters
	public Long getId() { return id; }
	public User getUser() { return user; }
	public Long getUserId() { return (user != null) ? user.getId() : null; }
	public Location getLocation() { return location; }
	public Long getLocationId() { return (location != null) ? location.getId() : null; }
	public Book getBook() { return book; }
	public String getIsbn13() { return (book != null) ? book.getIsbn13() : null; }
	public String getComment() { return comment; }
	public Status getStatus() { return status; }
	public Timestamp getUpdatedAt() { return updatedAt; }

	// Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
    	private Long id;
    	private User user;
    	private Location location;
    	private Book book;
    	private String comment;
    	private Status status;
    	private Timestamp updatedAt;
    	
    	public Builder id(Long id) {
            this.id = id;
            return this;
        }
    	
    	public Builder user(User user) {
            this.user = user;
            return this;
        }
    	
    	public Builder location(Location location) {
    		this.location = location;
    		return this;
    	}
    	
    	public Builder book(Book book) {
    		this.book = book;
    		return this;
    	}
    	
    	public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }
    	
    	public Builder status(Status status) {
            this.status = status;
            return this;
        }
    	
    	public Builder status(String status) {
            this.status = Status.valueOf(status.strip().toUpperCase());
            return this;
        }
    	
    	public Builder updatedAt(Timestamp updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
    	
    	public UserBook build() {
            return new UserBook(this);
        }
    }
}
