package com.social.bookshare.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Point;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.LocationRegisterRequest;
import com.social.bookshare.dto.request.LocationUpdateRequest;
import com.social.bookshare.dto.response.UserLocationReponse; // Added import
import com.social.bookshare.repository.LocationRepository;
import com.social.bookshare.repository.UserBookRepository;
import com.social.bookshare.service.LocationService;
import com.social.bookshare.utils.EntityMapper;
import com.social.bookshare.utils.GeometryUtils;
import com.social.bookshare.utils.UserRoleUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class LocationServiceImpl implements LocationService {

	private final LocationRepository locationRepository;
	private final UserBookRepository userBookRepository;
    
    public LocationServiceImpl(LocationRepository locationRepository, UserBookRepository userBookRepository) {
    	this.locationRepository = locationRepository;
    	this.userBookRepository = userBookRepository;
    }
    
    @Override
    @Transactional(readOnly = true)
	public List<UserLocationReponse> getUserLocations(Long userId) {
		return locationRepository.findByUser(EntityMapper.getReference(User.class, userId)).stream()
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
		return locationRepository.findByUserAndLabel(EntityMapper.getReference(User.class, userId), label)
				.orElseThrow(() -> new EntityNotFoundException("Location not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public Location getUserLocation(Long userId, double lat, double lon) {
		Point location = GeometryUtils.createPoint(lon, lat);
		
		return locationRepository.findByUserAndLocation(EntityMapper.getReference(User.class, userId), location)
				.orElseThrow(() -> new EntityNotFoundException("Location not found"));
	}
    
    @Override
    @Transactional
    public Location registerUserLocation(Long userId, LocationRegisterRequest request) {
    	Location userLocation = Location.builder()
    			.user(EntityMapper.getReference(User.class, userId))
    			.label(request.getLabel())
    			.address(request.getAddress())
    			.location(request.getUserLat(), request.getUserLon())
    			.isActive(request.isActive())
    			.build();
    	
    	return locationRepository.save(userLocation);
    }
    
    @Override
    @Transactional
    public Location registerUserLocation(Long userId, String label, String address, double userLat, double userLon, boolean isActive) {
    	Location userLocation = Location.builder()
    			.user(EntityMapper.getReference(User.class, userId))
    			.label(label)
    			.address(address)
    			.location(userLat, userLon)
    			.isActive(isActive)
    			.build();
    	
    	return locationRepository.save(userLocation);
    }
    
    @Override
    @Transactional
    public void updateUserLocation(Long userId, LocationUpdateRequest request) {
    	Location location = locationRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));
    	
    	if (location.getUserId() != userId || !UserRoleUtils.isUser()) { // Credential check
    		throw new BadCredentialsException("Illegal access: " + userId);
    	} else if (!userBookRepository.existsByLocationIdAndLoanerIsNull(request.getId())) { // Occupied by loan check
    		throw new IllegalArgumentException("The location info occupied by a book currently on loan cannot be changed.");
    	}
    	
//    	location.updateLocation(request.getLabel(), request.getUserLat(), request.getUserLon(), request.getIsActive());
    	location.updateLocation(request.getLabel(), request.getIsActive());
    }

	@Override
	@Transactional
	public void deleteUserLocation(Long userId, Long locationId) {
		Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found"));
		
		if (location.getUserId() != userId || !UserRoleUtils.isUser()) { // Credential check
			throw new BadCredentialsException("Illegal access: " + userId);
		} else if (!userBookRepository.existsByLocationIdAndLoanerIsNull(locationId)) { // Occupied by loan check
    		throw new IllegalArgumentException("The location info occupied by a book currently on loan cannot be changed.");
    	}
		
		locationRepository.delete(location);
	}
}
