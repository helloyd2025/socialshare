package com.social.bookshare.service;

import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.TotpVerificationRequest;
import com.social.bookshare.dto.response.TotpSetupResponse;

public interface TotpService {
	
	static final String rawKeyHint = "RKH:";

	public User authenticateTfa(Long userId, String code);	
	public TotpSetupResponse setupTfa(Long userId);
	public void verifyTfa(Long userId, TotpVerificationRequest request);
	public void disableTfa(Long userId);
}