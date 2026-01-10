package com.social.bookshare.dto.request;

public class TwoFactorAuthRequest {
	
	public TwoFactorAuthRequest(String email, String code) {
		this.email = email;
		this.code = code;
	}

    private String email;
    private String code;

    // Getters
	public String getEmail() { return email; }
    public String getCode() { return code; }
}
