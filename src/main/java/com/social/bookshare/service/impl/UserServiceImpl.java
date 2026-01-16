package com.social.bookshare.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.domain.UserKey;
import com.social.bookshare.dto.request.AuthenticateRequest;
import com.social.bookshare.dto.request.PassUpdateRequest;
import com.social.bookshare.dto.request.SignupRequest;
import com.social.bookshare.dto.request.TwoFactorAuthRequest;
import com.social.bookshare.repository.UserKeyRepository;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.TotpService;
import com.social.bookshare.service.UserService;
import com.social.bookshare.utils.EncryptionUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private final UserKeyRepository userKeyRepository;
	private final TotpService totpService;
	private final PasswordEncoder passwordEncoder;
	private final RedissonClient redissonClient;
	
	private static final String PASSWORD_PREFIX = "PASSWORD:";
	
	public UserServiceImpl(UserRepository userRepository, UserKeyRepository userKeyRepository, TotpService totpService, 
			PasswordEncoder passwordEncoder, RedissonClient redissonClient) {
		this.userRepository = userRepository;
		this.userKeyRepository = userKeyRepository;
		this.totpService = totpService;
		this.passwordEncoder = passwordEncoder;
		this.redissonClient = redissonClient;
	}

	@Override
	@Transactional(readOnly = true)
	public User authenticatePlainLevel(AuthenticateRequest request, Role role) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Email not found"));
		
		if (role != user.getRole()) {
			throw new AccessDeniedException("Illegal role access"); // Role check
		} else if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BadCredentialsException("Illegal pass access"); // Pass check
		}
		
		if (user.isTfaEnabled()) {
			RBucket<String> passwordBucket = redissonClient.getBucket(PASSWORD_PREFIX + user.getId());
			passwordBucket.set(request.getPassword(), Duration.ofMinutes(3));
		}
	    
		return user;
	}
	
	@Override
	@Transactional(readOnly = true)
	public User authenticateTwoFactorLevel(TwoFactorAuthRequest request, Role role) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Email not found"));
		
		if (!user.isTfaEnabled()) {
			throw new AccessDeniedException("2FA is not enabled for this user.");
		} else if (role != user.getRole()) {
			throw new AccessDeniedException("Illegal role access");
		}
		
		RBucket<String> passwordBucket = redissonClient.getBucket(PASSWORD_PREFIX + user.getId());
	    String password = passwordBucket.get();
	    
	    if (password == null) {
	        throw new BadCredentialsException("Expired or revoked request. Please login again.");
	    }
		try {
			String decryptedSecret = EncryptionUtils.decrypt(user.getTfaSecret(), password, user.getDecodedSalt());
			
	        if (!totpService.matches(decryptedSecret, request.getCode())) {
	            throw new BadCredentialsException("Invalid 2FA code");
	        }
		} catch (Exception e) {
	        throw new BadCredentialsException("Verification failed");
	    } finally {
	    	passwordBucket.delete();
	    }
		
		return user;
	}

	@Override
	@Transactional
	public User signup(SignupRequest request, Role role) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalStateException("Duplicate email");
		} else if (userRepository.existsByName(request.getName())) {
			throw new IllegalStateException("Duplicate name");
		}
		
		// Generate unique salt value
		byte[] salt = new byte[16];
	    new SecureRandom().nextBytes(salt);
	    String base64Salt = Base64.getEncoder().encodeToString(salt);
		
		User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .encryptionSalt(base64Salt)
                .build();
		
		return userRepository.save(user);
	}

	@Override
	@Transactional
	public void updatePassword(Long userId, Role role, PassUpdateRequest request) {
		User user = userRepository.findById(userId)
	            .orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		if (role != user.getRole()) {
			throw new AccessDeniedException("Illegal role access");
		} else if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
	        throw new BadCredentialsException("Illegal pass access");
	    }
		
		try { // Re-encrypt which encrypted by password
			// 2FA secret
			if (user.isTfaEnabled() && user.getTfaSecret() != null) {
				String rawTfaSecret = EncryptionUtils.decrypt(user.getTfaSecret(), request.getCurrentPassword(), user.getDecodedSalt());
				String reEncryptedSecret = EncryptionUtils.encrypt(rawTfaSecret, request.getNewPassword(), user.getDecodedSalt());
				user.updateTfaSecret(reEncryptedSecret);
			}
			// API keys
			List<UserKey> userKeys = userKeyRepository.findByUser(user);
			for (UserKey userKey : userKeys) {
	            String rawKey = EncryptionUtils.decrypt(userKey.getKey(), request.getCurrentPassword(), user.getDecodedSalt());
	            String reEncryptedKey = EncryptionUtils.encrypt(rawKey, request.getNewPassword(), user.getDecodedSalt());
	            userKey.updateKey(reEncryptedKey);
		    }
		} catch (Exception e) {
            throw new RuntimeException("Failed to re-encrypt data during password update", e);
        }
		
		String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
		user.updatePassword(encodedNewPassword);
	}
}
