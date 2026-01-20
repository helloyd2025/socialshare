package com.social.bookshare.service.impl;

import java.security.SecureRandom;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.config.security.JwtTokenProvider;
import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.AuthenticateRequest;
import com.social.bookshare.dto.request.PassUpdateRequest;
import com.social.bookshare.dto.request.SignupRequest;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.TotpService;
import com.social.bookshare.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private final TotpService totpService;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
	public UserServiceImpl(UserRepository userRepository, TotpService totpService, 
			PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.totpService = totpService;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	@Transactional(readOnly = true)
	public User authenticatePlainLevel(AuthenticateRequest request, Role role) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Email not found"));
		
		if (role != user.getRole()) {
			throw new AccessDeniedException("Illegal role access");
		} else if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BadCredentialsException("Illegal pass access");
		}
		
		return user;
	}
	
	@Override
	@Transactional(readOnly = true)
	public User authenticateTwoFactorLevel(String preAuthToken, String totpCode) {
		if (!jwtTokenProvider.validatePreAuthToken(preAuthToken)) {
			throw new AccessDeniedException("Invalid or expired pre-authentication token.");
		}

		Long userId = jwtTokenProvider.getUserId(preAuthToken);
    	User authenticatedUser = totpService.authenticateTfa(userId, totpCode);
    	
    	return authenticatedUser;
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
		
		String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
		user.updatePassword(encodedNewPassword);
	}
}
