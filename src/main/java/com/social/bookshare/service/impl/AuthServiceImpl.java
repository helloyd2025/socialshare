package com.social.bookshare.service.impl;

import java.time.Duration;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.config.security.JwtTokenProvider;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.TwoFactorAuthRequest;
import com.social.bookshare.dto.response.TokenResponse;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.AuthService;
import com.social.bookshare.service.TotpService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final TotpService totpService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedissonClient redissonClient;
	
	private static final String REFRESH_TOKEN_PREFIX = "REFRESH_TOKEN:";

	public AuthServiceImpl(UserRepository userRepository, TotpService totpService, 
			JwtTokenProvider jwtTokenProvider, RedissonClient redissonClient) {
		this.userRepository = userRepository;
		this.totpService = totpService;
		this.jwtTokenProvider = jwtTokenProvider;
		this.redissonClient = redissonClient;
	}
	
	@Value("${jwt.refreshTokenValidTime}")
    private long refreshTokenValidTime;
	
	@Override
	public TokenResponse issueTokensForPlainAuth(User user) {
		if (user.isTfaEnabled())  // 2FA check
			return TokenResponse.tfaRequired(); // If 2FA is enabled, tokens should never be issued.
		
		return this.createTokens(user);
	}
	
	@Override
	@Transactional(readOnly = true)
	public TokenResponse issueTokensForTwoFactorAuth(TwoFactorAuthRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("User not fonud"));
		
		if (!user.isTfaEnabled()) {
			throw new AccessDeniedException("2FA is not enabled for this user.");
		} else if (!totpService.match(user.getTfaSecret(), request.getCode())) {
			throw new BadCredentialsException("Invalid 2FA code");
		}
		
		return this.createTokens(user);
	}
	
	@Override
	@Transactional(readOnly = true)
	public TokenResponse reissueTokens(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) {
			throw new AccessDeniedException("Refresh Token expired. Log in again, please.");
		}
		
		Long userId = jwtTokenProvider.getUserId(refreshToken);
		
		RBucket<String> refreshTokenBucket = redissonClient.getBucket(REFRESH_TOKEN_PREFIX + userId);
	    String savedToken = refreshTokenBucket.get();
		
	    if (savedToken == null || !savedToken.equals(refreshToken)) {
	    	throw new AccessDeniedException("Invalid or revoked token");
	    }

	    User user = userRepository.findById(userId) // Data required immediately
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
