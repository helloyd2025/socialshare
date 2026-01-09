package com.social.bookshare.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationUpdateRequest {

	private Long id;
	private String label;
	
//	@JsonProperty("user_lat")
//	private double userLat;
//	
//	@JsonProperty("user_lon")
//	private double userLon;
	
	@JsonProperty("is_active")
	private boolean isActive;

	public Long getId() { return id; }
	public String getLabel() { return label; }
//	public double getUserLat() { return userLat; }
//	public double getUserLon() { return userLon; }
	public boolean getIsActive() { return isActive; }
}
