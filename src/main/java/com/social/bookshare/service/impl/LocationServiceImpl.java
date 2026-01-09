package com.social.bookshare.service.impl;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.LocationRegisterRequest;
import com.social.bookshare.dto.request.LocationUpdateRequest;
import com.social.bookshare.repository.LocationRepository;
import com.social.bookshare.service.LocationService;
import com.social.bookshare.utils.EntityMapper;
import com.social.bookshare.utils.GeometryUtils;
import com.social.bookshare.utils.UserRoleUtils;

@Service
public class LocationServiceImpl implements LocationService {

	private final LocationRepository locationRepository;
    
    public LocationServiceImpl(LocationRepository locationRepository) {
    	this.locationRepository = locationRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
	public List<Location> getUserLocations(Long userId) {
		return locationRepository.findByUser(EntityMapper.getReference(User.class, userId));
	}
    
    @Override
    @Transactional(readOnly = true)
	public Location getUserLocation(Long userId, String label) {
		return locationRepository.findByUserAndLabel(EntityMapper.getReference(User.class, userId), label)
				.orElseThrow(() -> new IllegalArgumentException("Location not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public Location getUserLocation(Long userId, double lat, double lon) {
		Point location = GeometryUtils.createPoint(lon, lat);
		
		return locationRepository.findByUserAndLocation(EntityMapper.getReference(User.class, userId), location)
				.orElseThrow(() -> new IllegalArgumentException("Location not found"));
	}
    
    @Override
    @Transactional
    public Long registerUserLocation(Long userId, LocationRegisterRequest request) {
    	Location userLocation = Location.builder()
    			.user(EntityMapper.getReference(User.class, userId))
    			.label(request.getLabel())
    			.address(request.getAddress())
    			.location(request.getUserLat(), request.getUserLon())
    			.isActive(request.getIsActive())
    			.build();
    	
    	return locationRepository.save(userLocation).getId();
    }
    
    @Override
    @Transactional
    public Long registerUserLocation(Long userId, String label, String address, double userLat, double userLon, boolean isActive) {
    	Location userLocation = Location.builder()
    			.user(EntityMapper.getReference(User.class, userId))
    			.label(label)
    			.address(address)
    			.location(userLat, userLon)
    			.isActive(isActive)
    			.build();
    	
    	return locationRepository.save(userLocation).getId();
    }
    
    @Override
    @Transactional
    public void updateUserLocation(Long userId, LocationUpdateRequest request) {
    	Location location = locationRepository.findById(request.getId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
    	
    	if (location.getUserId() != userId || !UserRoleUtils.isUser())
    		throw new BadCredentialsException("Illegal access: " + userId);
    	
//    	location.updateLocation(request.getLabel(), request.getUserLat(), request.getUserLon(), request.getIsActive());
    	location.updateLocation(request.getLabel(), request.getIsActive());
    	
    }

	@Override
	@Transactional
	public void deleteUserLocation(Long userId, Long locationId) {
		Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
		
		if (location.getUserId() != userId || !UserRoleUtils.isUser())
    		throw new BadCredentialsException("Illegal access: " + userId);
		
		locationRepository.delete(location);
	}
}
