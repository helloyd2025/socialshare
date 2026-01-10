package com.social.bookshare.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookLocationResponse {
	
	public BookLocationResponse(String label, String address, double lon, double lat, double distance) {
		this.label = label;
		this.address = address;
		this.lon = lon;
		this.lat = lat;
		this.distance = distance;
	}
	
	private BookLocationResponse(Builder builder) {
		this.type = builder.type;
		this.label = builder.label;
		this.address = builder.address;
		this.distance = builder.distance;
		this.lat = builder.lat;
		this.lon = builder.lon;
		this.isHighlight = builder.isHighlight;
	}

	private String type;
    private String label;
    private String address;
    private double distance;
    private double lat;
    private double lon;
    
    @JsonProperty("is_highlight")
    private boolean isHighlight;
    
    // Getters
    public String getType() { return type; }
	public String getLabel() { return label; }
	public String getAddress() { return address; }
	public double getDistance() { return distance; }
	public double getLat() { return lat; }
	public double getLon() { return lon; }
	public boolean isHighlight() { return isHighlight; }
	
	//Builder
	public static Builder builder() {
        return new Builder();
    }
	
	public static class Builder {
		private String type;
	    private String label;
	    private String address;
	    private double distance;
	    private double lat;
	    private double lon;
	    private boolean isHighlight;
	    
	    public Builder type(String type) {
            this.type = type;
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
	    
	    public Builder distance(double distance) {
            this.distance = distance;
            return this;
        }
	    
	    public Builder lat(double lat) {
            this.lat = lat;
            return this;
        }
	    
	    public Builder lon(double lon) {
            this.lon = lon;
            return this;
        }
	    
	    public Builder isHighlight(boolean isHighlight) {
            this.isHighlight = isHighlight;
            return this;
        }
	    
	    public BookLocationResponse build() {
            return new BookLocationResponse(this);
        }
	}
}
