package com.social.bookshare.service.impl;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.AuthenticateRequest;
import com.social.bookshare.dto.request.PassUpdateRequest;
import com.social.bookshare.dto.request.SignupRequest;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public User authenticate(AuthenticateRequest request, Role role) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Email not found"));
		
		if (role != user.getRole()) {
			throw new AccessDeniedException("Illegal role access"); // Role check
		} else if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BadCredentialsException("Illegal pass access"); // Pass check
		} else
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
		
		User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
		
		return userRepository.save(user);
	}

//	@Override
//	public TokenResponse issueTokens(User user) {
//		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
//		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
//		
//		return TokenResponse.success(accessToken, refreshToken);
//	}

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
		user.updateUserPassword(encodedNewPassword);
	}
}
