package com.social.bookshare.domain;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import com.social.bookshare.utils.GeometryUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "user_locations")
public class Location {

	protected Location() {}
	
	public Location(Builder builder) {
		this.id = builder.id;
		this.user = builder.user;
		this.label = builder.label;
		this.address = builder.address;
		this.location = builder.location;
		this.isActive = builder.isActive;
		this.createdAt = builder.createdAt;
	}
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private User user;
	
	@Size(max = 50)
	@Column(length = 50)
	private String label; // home, college, work etc
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String address;
	
	@Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;
	
	@Column(name = "is_active", nullable = false)
	private boolean isActive = true; // default true
	
	@CreationTimestamp
	@Column(name = "created_at")
	private Timestamp createdAt;
	
//	public void updateLocation(String label, double userLat, double userLon, boolean isActive) {
//		this.label = label.strip();
//		this.isActive = isActive;
//		this.location = GeometryUtils.createPoint(userLon, userLat);
//	}
	public void updateLocation(String label, boolean isActive) {
		this.label = label.strip();
		this.isActive = isActive;
	}
	
	// Getters
	public Long getId() { return id; }
	public User getUser() { return user; }
	public Long getUserId() { return (user != null) ? user.getId() : null; }
	public String getLabel() { return label; }
	public String getAddress() { return address; }
	public Point getLocation() { return location; }
	public boolean isActive() { return isActive; }
	public Timestamp getCreatedAt() { return createdAt; }

	// Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
    	private Long id;
    	private User user;
    	private String label;
    	private String address;
    	private Point location;
    	private boolean isActive;
    	private Timestamp createdAt;
    	
    	public Builder id(Long id) {
            this.id = id;
            return this;
        }
    	
    	public Builder user(User user) {
            this.user = user;
            return this;
        }
    	
    	public Builder label(String label) {
            this.label = label.strip();
            return this;
        }
    	
    	public Builder address(String address) {
            this.address = address.strip();
            return this;
        }
    	
    	public Builder location(Point location) {
    		this.location = location;
            return this;
        }
    	
    	public Builder location(double lat, double lon) { // lat-lon
    		this.location = GeometryUtils.createPoint(lon, lat); // lon-lat
            return this;
        }
    	
    	public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
    	
    	public Builder createdAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }
    	
    	public Location build() {
            return new Location(this);
        }
    }
}
