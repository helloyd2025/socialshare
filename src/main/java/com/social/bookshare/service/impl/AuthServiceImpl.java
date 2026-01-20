package com.social.bookshare.service.impl;

import java.time.Duration;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.config.security.JwtTokenProvider;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.response.TokenResponse;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.AuthService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedissonClient redissonClient;
	
	private static final String REFRESH_TOKEN_PREFIX = "REFRESH_TOKEN:";

	public AuthServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, RedissonClient redissonClient) {
		this.userRepository = userRepository;
		this.jwtTokenProvider = jwtTokenProvider;
		this.redissonClient = redissonClient;
	}
	
	@Value("${jwt.refreshTokenValidTime}")
    private long refreshTokenValidTime;
	
	@Override
	@Transactional(readOnly = true)
	public TokenResponse issueTokens(User user) {
		return this.createTokens(user);
	}
	
	@Override
	@Transactional
	public TokenResponse reissueTokens(String refreshToken) {
		if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
			throw new AccessDeniedException("Refresh Token expired. Log in again, please.");
		}
		
		Long userId = jwtTokenProvider.getUserId(refreshToken);
		
		RBucket<String> refreshTokenBucket = redissonClient.getBucket(REFRESH_TOKEN_PREFIX + userId);
	    String savedToken = refreshTokenBucket.get();
		
	    if (savedToken == null || !savedToken.equals(refreshToken)) {
	    	// If the provided refresh token does not match the one stored in Redis,
	    	// it indicates a potential token theft or reuse.
	    	// Invalidate all existing refresh tokens for this user by deleting the entry from Redis
	    	// to force a re-login on all devices.
	    	refreshTokenBucket.delete();
	    	throw new AccessDeniedException("Invalid or revoked token");
	    }

	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
		return this.createTokens(user);
	}
	
	private TokenResponse createTokens(User user) {
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        RBucket<String> refreshTokenBucket = redissonClient.getBucket(REFRESH_TOKEN_PREFIX + user.getId());
        refreshTokenBucket.set(refreshToken, Duration.ofMillis(refreshTokenValidTime));
        
        return TokenResponse.success(accessToken, refreshToken);
	}
}
