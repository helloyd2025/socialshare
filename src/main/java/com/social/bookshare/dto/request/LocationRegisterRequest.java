package com.social.bookshare.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationRegisterRequest {

	@JsonProperty("user_lat")
	private double userLat;
	
	@JsonProperty("user_lon")
	private double userLon;
	
	private String label;
	private String address;
	
	@JsonProperty("is_active")
	private boolean isActive;
	
	public double getUserLat() { return userLat; }
	public double getUserLon() { return userLon; }
	public String getLabel() { return label; }
	public String getAddress() { return address; }
	public boolean getIsActive() { return isActive; }
}
