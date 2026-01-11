package com.social.bookshare.service;

import com.social.bookshare.dto.NotificationMessage;

public interface NotificationService {

	public void sendNotification(NotificationMessage message);
}
