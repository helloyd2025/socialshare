package com.social.bookshare.dto.response;

public class TokenResponse {

	private String accessToken;
	private String refreshToken;
    private String tokenType = "Bearer";
    
    public TokenResponse(String accessToken) {
    	this.accessToken = accessToken;
    }

    public TokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
}
