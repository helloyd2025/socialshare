package com.social.bookshare.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NotificationMessage {
	
	private NotificationMessage(Builder builder) {
		this.receiverId = builder.receiverId;
		this.type = builder.type;
		this.message = builder.message;
		this.content = builder.content;
		this.timestamp = builder.timestamp;
	}
	
	@JsonProperty("receiver_id")
	Long receiverId;
	
	String type; // LOAN_REQUEST, REJECTED, CHAT
    String title;
    String message;
    Map<String, String> content;
    LocalDateTime timestamp;
    
    // Getters
	public Long getReceiverId() { return receiverId; }
	public String getTitle() { return title; }
	public String getMessage() { return message; }
	public Map<String, String> getContent() { return content; }
	public String getType() { return type; }
	public LocalDateTime getTimestamp() { return timestamp; }
	
	// Builder
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		Long receiverId;
		Long senderId;
		String type;
	    String title;
	    String message;
	    Map<String, String> content;
	    LocalDateTime timestamp;
	    
	    public Builder receiverId(Long receiverId) {
	    	this.receiverId = receiverId;
	    	return this;
	    }
	    
	    public Builder senderId(Long senderId) {
	    	this.senderId = senderId;
	    	return this;
	    }
	    
	    public Builder type(String type) {
	    	this.type = type;
	    	return this;
	    }
	    
	    public Builder title(String title) {
	    	this.title = title;
	    	return this;
	    }
	    
	    public Builder message(String message) {
	    	this.message = message;
	    	return this;
	    }
	    
	    public Builder content(Map<String, String> content) {
	    	this.content = content;
	    	return this;
	    }
	    
	    public Builder timestamp(LocalDateTime timestamp) {
	    	this.timestamp = timestamp;
	    	return this;
	    }
	    
	    public NotificationMessage build() {
	    	return new NotificationMessage(this);
	    }
	}
}
