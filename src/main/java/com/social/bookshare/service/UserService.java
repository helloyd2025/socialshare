package com.social.bookshare.service;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.AuthenticateRequest;
import com.social.bookshare.dto.request.PassUpdateRequest;

public interface UserService {

	public User authenticate(AuthenticateRequest request, Role role);
	public User signup(AuthenticateRequest request, Role role);
	
//	public TokenResponse issueTokens(User user);
	
	public void updatePassword(Long userId, Role role, PassUpdateRequest request);
}
