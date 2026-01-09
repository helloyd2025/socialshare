package com.social.bookshare.service;

import java.util.List;

import com.social.bookshare.domain.Location;
import com.social.bookshare.dto.request.LocationRegisterRequest;
import com.social.bookshare.dto.request.LocationUpdateRequest;

public interface LocationService {
	
	public List<Location> getUserLocations(Long userId);
	public Location getUserLocation(Long userId, String label);
	public Location getUserLocation(Long userId, double lat, double lon);

	public Long registerUserLocation(Long userId, LocationRegisterRequest request);
	public Long registerUserLocation(Long userId, String label, String address, double userLat, double userLon, boolean isActive);
	
	public void updateUserLocation(Long userId, LocationUpdateRequest request);
	public void deleteUserLocation(Long userId, Long locationId);
}
