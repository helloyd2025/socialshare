package com.social.bookshare.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.LocationRegisterRequest;
import com.social.bookshare.dto.request.LocationUpdateRequest;
import com.social.bookshare.dto.response.UserLocationReponse;
import com.social.bookshare.repository.LocationRepository;
import com.social.bookshare.repository.UserBookRepository;
import com.social.bookshare.service.LocationService;
import com.social.bookshare.utils.EntityMapper;
import com.social.bookshare.utils.GeometryUtils;
import com.social.bookshare.utils.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class LocationServiceImpl implements LocationService {

	private final LocationRepository locationRepository;
	private final UserBookRepository userBookRepository;
	private final GeometryUtils geometryUtils;
	private final EntityMapper entityMapper;
    
    public LocationServiceImpl(LocationRepository locationRepository, UserBookRepository userBookRepository, 
    		GeometryUtils geometryUtils, EntityMapper entityMapper) {
    	this.locationRepository = locationRepository;
    	this.userBookRepository = userBookRepository;
    	this.geometryUtils = geometryUtils;
    	this.entityMapper = entityMapper;
    }
    
    @Override
    @Transactional(readOnly = true)
	public List<UserLocationReponse> getUserLocations(Long userId) {
		return locationRepository.findByUser(entityMapper.getReference(User.class, userId)).stream()
				.<UserLocationReponse>map(l -> UserLocationReponse.builder()
						.id(l.getId())
						.label(l.getLabel())
						.address(l.getAddress())
						.location(l.getLocation())
						.isActive(l.isActive())
						.createdAt(l.getCreatedAt())
						.build())
				.collect(Collectors.toList());
	}
    
    @Override
    @Transactional(readOnly = true)
	public Location getUserLocation(Long userId, String label) {
		return locationRepository.findByUserAndLabel(entityMapper.getReference(User.class, userId), label)
				.orElseThrow(() -> new EntityNotFoundException("Location not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public Location getUserLocation(Long userId, double lat, double lon) {
		Point location = geometryUtils.createPoint(lon, lat);
		
		return locationRepository.findByUserAndLocation(entityMapper.getReference(User.class, userId), location)
				.orElseThrow(() -> new EntityNotFoundException("Location not found"));
	}
    
    @Override
    @Transactional
    public Location registerUserLocation(Long userId, LocationRegisterRequest request) {
    	Location userLocation = Location.builder()
    			.user(entityMapper.getReference(User.class, userId))
    			.label(request.getLabel())
    			.address(request.getAddress())
    			.location(geometryUtils.createPoint(request.getUserLon(), request.getUserLat()))
    			.isActive(request.isActive())
    			.build();
    	
    	return locationRepository.save(userLocation);
    }
    
    @Override
    @Transactional
    public Location registerUserLocation(Long userId, String label, String address, double userLat, double userLon, boolean isActive) {
    	Location userLocation = Location.builder()
    			.user(entityMapper.getReference(User.class, userId))
    			.label(label)
    			.address(address)
    			.location(geometryUtils.createPoint(userLon, userLat))
    			.isActive(isActive)
    			.build();
    	
    	return locationRepository.save(userLocation);
    }
    
    @Override
    @Transactional
    public void updateUserLocation(Long userId, LocationUpdateRequest request) {
    	Location location = locationRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));
    	
    	if (location.getUserId() != userId || !SecurityUtils.isUser()) { // Credential check
    		throw new AccessDeniedException("Illegal access: " + userId);
    	} else if (userBookRepository.isLocationOccupied(request.getId())) { // Occupied by loan
    		throw new IllegalStateException("The location info occupied by a book currently on loan cannot be changed.");
    	}
    	
//    	location.updateLocation(request.getLabel(), request.getUserLat(), request.getUserLon(), request.getIsActive());
    	location.updateLocation(request.getLabel(), request.getIsActive());
    }

	@Override
	@Transactional
	public void deleteUserLocation(Long userId, Long locationId) {
		Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));
		
		if (location.getUserId() != userId || !SecurityUtils.isUser()) { // Credential check
			throw new AccessDeniedException("Illegal access: " + userId);
		} else if (userBookRepository.isLocationOccupied(locationId)) { // Occupied by loan
    		throw new IllegalStateException("The location info occupied by a book currently on loan cannot be changed.");
    	}
		
		locationRepository.delete(location);
	}
}
