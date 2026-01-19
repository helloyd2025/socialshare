package com.social.bookshare.service.impl;

import java.security.SecureRandom;
import java.time.Duration;
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
import com.social.bookshare.dto.DecryptionResult;
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
	
	private static final String TEMP_PASSWORD_PREFIX = "TEMP_PASSWORD:";
	
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
	    
		if (user.isTfaEnabled()) { // Prepare 2FA
			try {
				String protectedPwd = EncryptionUtils.encryptWithSystemKey(request.getPassword());
		        RBucket<String> pwdBucket = redissonClient.getBucket(TEMP_PASSWORD_PREFIX + user.getId());
		        pwdBucket.set(protectedPwd, Duration.ofMinutes(3));
			} catch (Exception e) {
				throw new RuntimeException("Secure session preparation failed");
			}
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
		RBucket<String> pwdBucket = redissonClient.getBucket(TEMP_PASSWORD_PREFIX + user.getId());
		String rawPassword;
		
		try {
	        rawPassword = EncryptionUtils.decryptWithSystemKey(pwdBucket.get());
	    } catch (Exception e) {
	        throw new BadCredentialsException("Invalid or expired request");
	    }
		try {
			DecryptionResult rawSecretResult = EncryptionUtils.decryptFlexibly(rawPassword, user.getDecodedSalt(), user.getTfaSecret());
	        
	        if (!totpService.matches(rawSecretResult.getPlainText(), request.getCode())) {
	        	throw new BadCredentialsException("Invalid 2FA code");
	        }
	        if (rawSecretResult.isUpgradeRequired()) {
	            String upgradedSecret = EncryptionUtils.encryptHybrid(rawPassword, user.getDecodedSalt(), rawSecretResult.getPlainText());
	            user.updateTfaSecret(upgradedSecret);
	        }
	        pwdBucket.delete();
			return user;
			
		} catch (BadCredentialsException e) {
	        throw new BadCredentialsException("Verification failed: " + e.getMessage());
	    } catch (Exception e) {
	        throw new RuntimeException("Verification failed");
	    }
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
//	    String base64Salt = Base64.getEncoder().encodeToString(salt);
		
		User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .encryptionSalt(salt)
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
		
		try {
			// [1] UserKey(API Keys)
			List<UserKey> userKeys = userKeyRepository.findByUser(user);
			for (UserKey userKey : userKeys) {
	            String rawUserKey = EncryptionUtils.decryptHybrid(request.getCurrentPassword(), user.getDecodedSalt(), userKey.getKey());
	            String reEncryptedUserKey = EncryptionUtils.encryptHybrid(request.getNewPassword(), user.getDecodedSalt(), rawUserKey);
	            userKey.updateKey(reEncryptedUserKey);
		    }
			// [2] 2FA Secret
	        if (user.getTfaSecret() != null) {
	            DecryptionResult rawSecretResult = EncryptionUtils
	            		.decryptFlexibly(request.getCurrentPassword(), user.getDecodedSalt(), user.getTfaSecret());
	            String reEncryptedSecret = EncryptionUtils
	            		.encryptHybrid(request.getNewPassword(), user.getDecodedSalt(), rawSecretResult.getPlainText());
	            user.updateTfaSecret(reEncryptedSecret);
	        }
		} catch (Exception e) {
            throw new RuntimeException("Failed to re-encrypt data during password update", e);
        }
		
		String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
		user.updatePassword(encodedNewPassword);
	}
}
