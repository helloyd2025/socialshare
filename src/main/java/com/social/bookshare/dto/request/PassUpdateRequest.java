package com.social.bookshare.dto.request;

public class PassUpdateRequest {

	private String currentPassword;
    private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
}
