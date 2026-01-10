package com.social.bookshare.dto.response;

import java.time.LocalDateTime;

import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserLocationReponse {

	public UserLocationReponse() {}
	
	private UserLocationReponse(Builder builder) {
		Point location = builder.location;
		
		this.id = builder.id;
		this.label = builder.label;
		this.address = builder.address;
		this.lon = location.getX();
		this.lat = location.getY();
		this.isActive = builder.isActive;
		this.createdAt = builder.createdAt;
	}
	
	private Long id;
	private String label;
	private String address;
	private double lon;
	private double lat;
	
	@JsonProperty("is_active")
	private boolean isActive;
	
	@JsonProperty("created_at")
	private LocalDateTime createdAt;

	// Getters
	public Long getId() { return id; }
	public String getLabel() { return label; }
	public String getAddress() { return address; }
	public double getLon() { return lon; }
	public double getLat() { return lat; }
	public boolean isActive() { return isActive; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	
	//Builder
	public static Builder builder() {
        return new Builder();
    }
	
	public static class Builder {
		private Long id;
		private String label;
		private String address;
		private Point location;
		private boolean isActive;
		private LocalDateTime createdAt;
		
		public Builder id(Long id) {
			this.id = id;
			return this;
		}
	    
	    public Builder label(String label) {
            this.label = label;
            return this;
	    }
	    
	    public Builder address(String address) {
            this.address = address;
            return this;
        }
	    
	    public Builder location(Point location) {
            this.location = location;
            return this;
        }
	    
	    public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
	    
	    public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
	    }
	    
	    public UserLocationReponse build() {
            return new UserLocationReponse(this);
        }
	}
}
