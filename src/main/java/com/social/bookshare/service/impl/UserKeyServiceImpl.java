package com.social.bookshare.service.impl;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserKey;
import com.social.bookshare.dto.request.UserKeyRegisterRequest;
import com.social.bookshare.repository.UserKeyRepository;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.UserKeyService;
import com.social.bookshare.utils.EncryptionUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UserKeyServiceImpl implements UserKeyService {
	
	private final UserKeyRepository userKeyRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserKeyServiceImpl(UserKeyRepository userKeyRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userKeyRepository = userKeyRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public UserKey registerKey(Long userId, UserKeyRegisterRequest request) throws Exception {
		User user = userRepository.findById(userId)
	            .orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new BadCredentialsException("Invalid password");
	    }

	    byte[] salt = user.getDecodedSalt();
	    String encryptedKey = EncryptionUtils.encrypt(request.getApiKey(), request.getPassword(), salt);

	    UserKey userKey = UserKey.builder()
	    		.user(user)
	    		.type(request.getType())
	    		.key(encryptedKey)
	    		.build();
	    		
	    return userKeyRepository.save(userKey);
	}
}
