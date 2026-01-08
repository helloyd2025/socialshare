package com.social.bookshare.service;

import com.social.bookshare.domain.Location;
import com.social.bookshare.dto.request.LocationRegisterRequest;

public interface LocationService {
	
	public Location getUserLocation(Long userId, String label);
	public Location getUserLocation(Long userId, double lat, double lon);

	public Long registerLocation(Long userId, LocationRegisterRequest request);
	public Long registerLocation(Long userId, String label, String address, double userLat, double userLon, boolean isActive);
}
