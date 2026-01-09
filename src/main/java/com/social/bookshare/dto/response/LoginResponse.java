package com.social.bookshare.dto.response;

public class LoginResponse {

    private boolean twoFactorRequired;
    private String accessToken;
    private String refreshToken;

    // 2FA가 필요할 때 사용하는 생성자
    public LoginResponse(boolean twoFactorRequired) {
        this.twoFactorRequired = twoFactorRequired;
        this.accessToken = null;
        this.refreshToken = null;
    }

    // 2FA가 필요 없을 때 사용하는 생성자
    public LoginResponse(boolean twoFactorRequired, String accessToken, String refreshToken) {
        this.twoFactorRequired = twoFactorRequired;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public boolean isTwoFactorRequired() {
        return twoFactorRequired;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
