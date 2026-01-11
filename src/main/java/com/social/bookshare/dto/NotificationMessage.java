package com.social.bookshare.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record NotificationMessage(
		Long receiverId,
	    String title,
	    String content,
	    String type, // LOAN_REQUEST, REJECTED, CHAT
	    LocalDateTime timestamp
	) implements Serializable {}
