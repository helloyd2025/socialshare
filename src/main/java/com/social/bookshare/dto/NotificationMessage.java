package com.social.bookshare.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public record NotificationMessage(
		Long receiverId,
	    String title,
	    String comment,
	    Map<String, String> content,
	    String type, // LOAN_REQUEST, REJECTED, CHAT
	    LocalDateTime timestamp
	) implements Serializable {}
