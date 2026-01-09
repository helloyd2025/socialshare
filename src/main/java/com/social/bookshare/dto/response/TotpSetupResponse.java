package com.social.bookshare.dto.response;

public class TotpSetupResponse {
	
	public TotpSetupResponse(String secret, String qrCodeUri) {
		this.secret = secret;
		this.qrCodeUri = qrCodeUri;
	}

	private String secret;
    private String qrCodeUri;
    
	public String getSecret() { return secret; }
	public String getQrCodeUri() { return qrCodeUri; }
}
