package com.social.bookshare.service;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.LoginRequest;
import com.social.bookshare.dto.request.SignupRequest;
import com.social.bookshare.dto.response.TokenResponse;

public interface UserService {

	public User login(LoginRequest request, Role role);
	public User signup(SignupRequest request, Role role);
	
	public TokenResponse issueTokens(User user);
}
