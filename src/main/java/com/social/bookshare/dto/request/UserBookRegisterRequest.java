package com.social.bookshare.dto.request;

import com.social.bookshare.domain.UserBook.Status;

public class UserBookRegisterRequest {
	
	private UserBookRegisterRequest(Builder builder) {
		this.locationId = builder.locationId;
		this.bookId = builder.bookId;
		this.comment = builder.comment;
		this.status = builder.status;
	}

	private Long locationId;
	private Long bookId;
	private String comment;
	private Status status;
	
	// Getters
	public Long getLocationId() { return locationId; }
	public Long getBookId() { return bookId; }
	public String getComment() { return comment; }
	public Status getStatus() { return status; }
	
	// Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
    	private Long locationId;
    	private Long bookId;
    	private String comment;
    	private Status status;
    	
    	public Builder locationId(Long locationId) {
    		this.locationId = locationId;
    		return this;
    	}
    	
    	public Builder bookId(Long bookId) {
    		this.bookId = bookId;
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
    	
    	public UserBookRegisterRequest build() {
            return new UserBookRegisterRequest(this);
        }
    }
}
