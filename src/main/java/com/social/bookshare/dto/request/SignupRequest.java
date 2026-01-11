package com.social.bookshare.dto.request;

public class SignupRequest {

	public SignupRequest(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
	}

	private String email;
	private String name;
    private String password;

    // Getters
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPassword() { return password; }
}
