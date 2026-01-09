package com.social.bookshare.dto.request;

public class UserBookUpdateRequest {

	private Long id;
	
	// user_locations
	private Long locationId;
	private double userLat;
	private double userLon;
	private String address;
	
	// user_books
	private String comment;
	private String status;
	
	// Getters
	public Long getId() { return id; }
	public Long getLocationId() { return locationId; }
	public double getUserLat() { return userLat; }
	public double getUserLon() { return userLon; }
	public String getAddress() { return address; }
	public String getComment() { return comment; }
	public String getStatus() { return status; }
	
	// Setters
	public void setLocationId(Long locationId) { this.locationId = locationId; }
}
