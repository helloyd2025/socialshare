package com.social.bookshare.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

	private String accessToken;
	private String refreshToken;
    private String tokenType;
    private boolean requiresTwoFactor;

    public TokenResponse(String accessToken, String refreshToken, boolean requiresTwoFactor) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.requiresTwoFactor = requiresTwoFactor;
        this.tokenType = (accessToken != null) ? "Bearer" : null;
    }
    
    public static TokenResponse tfaRequired() {
        return new TokenResponse(null, null, true);
    }
    
    public static TokenResponse success(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken, false);
    }
    
    public TokenResponse toPublicResponse() {
        return new TokenResponse(this.accessToken, null, this.requiresTwoFactor);
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
    public boolean requiresTwoFactor() { return requiresTwoFactor; }
}
