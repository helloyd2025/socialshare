package com.social.bookshare.service;

import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.TwoFactorAuthRequest;
import com.social.bookshare.dto.response.LoginResponse;
import com.social.bookshare.dto.response.TokenResponse;

public interface AuthService {

	public LoginResponse issueTokensAndCheck2FA(User user);
	public TokenResponse reissueTokens(String refreshToken);
	public TokenResponse authenticateWith2FA(TwoFactorAuthRequest request);
}
