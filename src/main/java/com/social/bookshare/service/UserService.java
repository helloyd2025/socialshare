package com.social.bookshare.service;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.AuthenticateRequest;
import com.social.bookshare.dto.request.PassUpdateRequest;
import com.social.bookshare.dto.request.SignupRequest;
import com.social.bookshare.dto.request.TwoFactorAuthRequest;

public interface UserService {

	public User authenticatePlainLevel(AuthenticateRequest request, Role role);
	public User authenticateTwoFactorLevel(TwoFactorAuthRequest request, Role role);
	
	public User signup(SignupRequest request, Role role);
	
	public void updatePassword(Long userId, Role role, PassUpdateRequest request);
}
