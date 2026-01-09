package com.social.bookshare.service.impl;

import java.time.Duration;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.config.security.JwtTokenProvider;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.TwoFactorAuthRequest;
import com.social.bookshare.dto.response.LoginResponse;
import com.social.bookshare.dto.response.TokenResponse;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.AuthService;
import com.social.bookshare.service.TotpService;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final TotpService totpService;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedissonClient redissonClient;

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
	public LoginResponse issueTokensAndCheck2FA(User user) {
		if (user.getTfaEnabled()) 
			return new LoginResponse(true); // Just respond 2FA required without tokens
		
		// If 2FA not required, tokens issued immediately.
		TokenResponse tokenResponse = createTokens(user);
		return new LoginResponse(false, tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
	}
	
	@Override
	@Transactional(readOnly = true)
	public TokenResponse reissueTokens(String refreshToken) {
		if (!jwtTokenProvider.validateToken(refreshToken)) 
            throw new RuntimeException("Refresh Token expired. Log in again, please.");
		
		Long userId = jwtTokenProvider.getUserId(refreshToken);
		
		RBucket<String> refreshTokenBucket = redissonClient.getBucket("RT:" + userId);
	    String savedToken = refreshTokenBucket.get();
		
	    if (savedToken == null || !savedToken.equals(refreshToken)) 
	        throw new RuntimeException("Invalid or revoked token");

	    User user = userRepository.findById(userId) // Required immediately
	            .orElseThrow(() -> new RuntimeException("User not found"));
        
		return createTokens(user);
	}
	
	@Override
	@Transactional(readOnly = true)
	public TokenResponse authenticateWith2FA(TwoFactorAuthRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Invalid credentials."));
		
		if (!user.getTfaEnabled()) {
			throw new BadCredentialsException("Two-factor authentication is not enabled for this user.");
		} else if (!totpService.match(user.getTfaSecret(), request.getCode())) {
			throw new BadCredentialsException("Invalid 2FA code.");
		}
		
		return createTokens(user);
	}
	
	private TokenResponse createTokens(User user) {
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        RBucket<String> refreshTokenBucket = redissonClient.getBucket("RT:" + user.getId());
        refreshTokenBucket.set(refreshToken, Duration.ofMillis(refreshTokenValidTime));
        
        return new TokenResponse(accessToken, refreshToken);
	}
}
