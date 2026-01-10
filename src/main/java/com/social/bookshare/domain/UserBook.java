package com.social.bookshare.domain;

import java.time.LocalDateTime;

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
	
	private UserBook(Builder builder) {
		this.id = builder.id;
		this.owner = builder.owner;
		this.loaner = null;
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
	@JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
	private User owner;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "loaner_id", referencedColumnName = "id")
	private User loaner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "location_id", referencedColumnName = "id", nullable = false)
	private Location location;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", referencedColumnName = "id", nullable = false)
    private Book book;
	
	@Column(columnDefinition = "TEXT")
	private String comment;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;
	
	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
	
	public enum Status {
		AVAILABLE, 
		PENDING_LOAN,   // 대출 신청 후 승낙 대기
	    LOANED,         // 대출 중
	    PENDING_RETURN, // 반납 신청 후 승낙 대기, 
		HIDDEN, EXPIRED, BANNED
	}
	
	public void updateUserBook(Location location, String comment, Status status) {
		this.location = location;
		this.comment = comment;
		this.status = status;
	}
	
	public boolean isNotLoaned() {
		return loaner == null;
	}
	
	public void markAsPendingLoan() {
		if (this.status != Status.AVAILABLE) throw new IllegalStateException("Unavailable book");
		this.status = Status.PENDING_LOAN;
	}
	public void markAsLoaned(User loaner) {
		this.loaner = loaner;
		this.status = Status.LOANED;
	}
	public void markAsPendingReturn() {
		this.status = Status.PENDING_RETURN;
	}
	public void markAsAvailable() {
		this.loaner = null;
		this.status = Status.AVAILABLE;
	}

	// Getters
	public Long getId() { return id; }
	public User getOwner() { return owner; }
	public Long getOwnerId() { return (owner != null) ? owner.getId() : null; }
	public User getLoaner() { return loaner; }
	public Long getLoanerId() { return (loaner != null) ? loaner.getId() : null; }
	public Location getLocation() { return location; }
	public Long getLocationId() { return (location != null) ? location.getId() : null; }
	public Book getBook() { return book; }
	public String getIsbn13() { return (book != null) ? book.getIsbn13() : null; }
	public String getComment() { return comment; }
	public Status getStatus() { return status; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }

	// Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
    	private Long id;
    	private User owner;
    	private Location location;
    	private Book book;
    	private String comment;
    	private Status status;
    	private LocalDateTime updatedAt;
    	
    	public Builder id(Long id) {
            this.id = id;
            return this;
        }
    	
    	public Builder owner(User owner) {
            this.owner = owner;
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
    	
    	public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
    	
    	public UserBook build() {
            return new UserBook(this);
        }
    }
}
