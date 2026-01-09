package com.social.bookshare.dto.request;

public class TwoFactorAuthRequest {

    private String email;
    private String code;
    
    public TwoFactorAuthRequest(String email, String code) {
		this.email = email;
		this.code = code;
	}

	public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
