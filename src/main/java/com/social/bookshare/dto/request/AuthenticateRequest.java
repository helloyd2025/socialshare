package com.social.bookshare.dto.request;

public class AuthenticateRequest {
	
	public AuthenticateRequest(String email, String password) {
		this.email = email;
		this.password = password;
	}

	private String email;
    private String password;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
