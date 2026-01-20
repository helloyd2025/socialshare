package com.social.bookshare.dto.request;

public class TwoFactorAuthRequest {
	
	public TwoFactorAuthRequest(String code) {
		this.code = code;
	}

    private String code;

    // Getters
    public String getCode() { return code; }
}
