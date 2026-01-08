package com.social.bookshare.service.impl;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.LocationRegisterRequest;
import com.social.bookshare.repository.LocationRepository;
import com.social.bookshare.service.LocationService;
import com.social.bookshare.utils.EntityMapper;
import com.social.bookshare.utils.GeometryUtils;

@Service
public class LocationServiceImpl implements LocationService {

	private final LocationRepository locationRepository;
	private final EntityMapper entityMapper;
    
    public LocationServiceImpl(LocationRepository locationRepository, EntityMapper entityMapper) {
    	this.locationRepository = locationRepository;
    	this.entityMapper = entityMapper;
    }
    
    @Override
    @Transactional(readOnly = true)
	public Location getUserLocation(Long userId, String label) {
		return locationRepository.findByUserIdAndLabel(userId, label)
				.orElseThrow(() -> new RuntimeException("Location not found"));
	}

	@Override
	@Transactional(readOnly = true)
	public Location getUserLocation(Long userId, double lat, double lon) {
		Point location = GeometryUtils.createPoint(lon, lat);
		
		return locationRepository.findByUserIdAndLocation(userId, location)
				.orElseThrow(() -> new RuntimeException("Location not found"));
	}
    
    @Override
    @Transactional
    public Long registerLocation(Long userId, LocationRegisterRequest request) {
    	Location userLocation = Location.builder()
    			.user(entityMapper.getReference(User.class, userId))
    			.label(request.getLabel())
    			.address(request.getAddress())
    			.location(request.getUserLat(), request.getUserLon())
    			.isActive(request.getIsActive())
    			.build();
    	
    	return locationRepository.save(userLocation).getId();
    }
    
    @Override
    @Transactional
    public Long registerLocation(Long userId, String label, String address, double userLat, double userLon, boolean isActive) {
    	Location userLocation = Location.builder()
    			.user(entityMapper.getReference(User.class, userId))
    			.label(label)
    			.address(address)
    			.location(userLat, userLon)
    			.isActive(isActive)
    			.build();
    	
    	return locationRepository.save(userLocation).getId();
    }
}
