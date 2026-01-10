package com.social.bookshare.service;

import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.TwoFactorAuthRequest;
import com.social.bookshare.dto.response.TokenResponse;

public interface AuthService {

	public TokenResponse issueTokensPlainAuth(User user);
	public TokenResponse reissueTokens(String refreshToken);
	public TokenResponse issueTokensTfaAuth(TwoFactorAuthRequest request);
}
