package com.social.bookshare.service.impl;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.social.bookshare.dto.NotificationMessage;
import com.social.bookshare.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {

	private final RedissonClient redissonClient;
	
    private static final String TOPIC_NAME = "USER_NOTIFICATIONS";
    
    public NotificationServiceImpl(RedissonClient redissonClient) {
    	this.redissonClient = redissonClient;
    }
	
	public void sendNotification(NotificationMessage message) {
		RTopic topic = redissonClient.getTopic(TOPIC_NAME);
        topic.publish(message);
	}
}
