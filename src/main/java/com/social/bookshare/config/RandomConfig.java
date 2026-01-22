package com.social.bookshare.config;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RandomConfig {

	@Bean
	SecureRandom secureRandom() {
		return new SecureRandom();
	}
}
