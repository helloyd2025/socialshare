package com.social.bookshare.service;

import com.social.bookshare.dto.request.TotpVerificationRequest;
import com.social.bookshare.dto.response.TotpSetupResponse;

public interface TotpService {
	
	static final String rawKeyHint = "RKH:";

	boolean matches(String secret, String code);
    TotpSetupResponse setupTfa(Long userId);
    void verifyTfa(Long userId, String code);
    void verifyTfa(Long userId, TotpVerificationRequest request);
    void disableTfa(Long userId);
}