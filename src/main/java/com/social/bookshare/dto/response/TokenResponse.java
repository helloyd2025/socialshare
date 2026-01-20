package com.social.bookshare.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {

	private String accessToken;
	private String refreshToken;
    private String preAuthToken;
    private String tokenType;
    private boolean requiresTwoFactor;

    public TokenResponse(String accessToken, String refreshToken, String preAuthToken, boolean requiresTwoFactor) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.preAuthToken = preAuthToken;
        this.requiresTwoFactor = requiresTwoFactor;
        this.tokenType = (accessToken != null || preAuthToken != null) ? "Bearer" : null;
    }
    
    public static TokenResponse tfaRequired(String preAuthToken) {
        return new TokenResponse(null, null, preAuthToken, true);
    }
    
    public static TokenResponse success(String accessToken, String refreshToken) {
        return new TokenResponse(accessToken, refreshToken, null, false);
    }
    
    public TokenResponse toPublicResponse() {
        return new TokenResponse(this.accessToken, null, this.preAuthToken, this.requiresTwoFactor);
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getPreAuthToken() { return preAuthToken; }
    public String getTokenType() { return tokenType; }
    public boolean isRequiresTwoFactor() { return requiresTwoFactor; }
}
