package com.social.bookshare.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnRequest {
	
	public ReturnRequest(Long ownerId, String comment) {
		this.ownerId = ownerId;
		this.comment = comment;
		this.requestedAt = LocalDateTime.now();
	}

	@JsonProperty("loaner_id")
	private Long ownerId;
	
	private String comment;
	
	@JsonProperty("requested_at")
	private LocalDateTime requestedAt;
	
	public Long getOwnerId() { return ownerId; }
	public String getComment() { return comment; }
	public LocalDateTime getRequestedAt() { return requestedAt; }
}
