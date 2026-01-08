package com.social.bookshare.service.impl;

import java.time.Duration;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.config.security.JwtTokenProvider;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.response.TokenResponse;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.AuthService;

public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedissonClient redissonClient;

	public AuthServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, RedissonClient redissonClient) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.redissonClient = redissonClient;
	}
	
	@Value("${jwt.refreshTokenValidTime}") // application.yml의 설정값 읽기
    private long refreshTokenValidTime;
	
	@Override
	public TokenResponse issueTokens(User user) {
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        RBucket<String> refreshTokenBucket = redissonClient.getBucket("RT:" + user.getId());
        refreshTokenBucket.set(refreshToken, Duration.ofMillis(refreshTokenValidTime));
        
        return new TokenResponse(accessToken, refreshToken);
	}
	
	@Override
	@Transactional(readOnly = true)
	public TokenResponse reissueTokens(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh Token expired. Log in again, please.");
        }
		
		Long userId = jwtTokenProvider.getUserId(refreshToken);
		
		RBucket<String> refreshTokenBucket = redissonClient.getBucket("RT:" + userId);
	    String savedToken = refreshTokenBucket.get();
		
	    if (savedToken == null || !savedToken.equals(refreshToken)) {
	        throw new RuntimeException("Invalid or revoked token");
	    }

	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("User not found"));
        
		return issueTokens(user);
	}
}
