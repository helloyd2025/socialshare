package com.social.bookshare.utils;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.social.bookshare.controller.NotificationController;
import com.social.bookshare.dto.NotificationMessage;

@Component
public class NotificationListener implements CommandLineRunner {
	
	private final RedissonClient redissonClient;
//	private final SimpMessagingTemplate messagingTemplate; // WebSocket
    private final NotificationController notificationController;
    
    public NotificationListener(RedissonClient redissonClient, NotificationController notificationController) {
    	this.redissonClient = redissonClient;
    	this.notificationController = notificationController;
    }

	@Override
	public void run(String... args) throws Exception {
		RTopic topic = redissonClient.getTopic("user-notifications");
        
        topic.addListener(NotificationMessage.class, (channel, msg) -> {
            // Receiving a message from Redis, forward it to a specific user via SSE.
            notificationController.sendToUser(msg.getReceiverId(), msg);
        });
	}

}
