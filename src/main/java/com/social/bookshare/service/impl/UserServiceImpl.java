package com.social.bookshare.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.config.security.JwtTokenProvider;
import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.LoginRequest;
import com.social.bookshare.dto.request.SignupRequest;
import com.social.bookshare.dto.response.TokenResponse;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.UserService;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	@Transactional(readOnly = true)
	public User login(LoginRequest request, Role role) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new IllegalArgumentException("Email not found"));
		
		if (role != user.getRole()) {
			throw new IllegalArgumentException("Illegal role access"); // Role check
		} else if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new IllegalArgumentException("Illegal password"); // Pass check
		} else
			return user;
	}

	@Override
	@Transactional
	public User signup(SignupRequest request, Role role) {
		if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Duplicate email");
        }
		
		User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
		
		return userRepository.save(user);
	}

	@Override
	public TokenResponse issueTokens(User user) {
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
		
		return new TokenResponse(accessToken, refreshToken);
	}
}
