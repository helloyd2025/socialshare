package com.social.bookshare.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserKeyRegisterRequest {

	String type;
	String password;
	
	@JsonProperty("api_key")
	String apiKey;
	
	public String getType() { return type; }
	public String getPassword() { return password; }
	public String getApiKey() { return apiKey; }
}
