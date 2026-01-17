package com.social.bookshare.dto.request;

public class TotpVerificationRequest {
	
	public TotpVerificationRequest(String code) {
		this.code = code;
	}

	private String code;
	
	public String getCode() { return code; }
}
