package com.social.bookshare.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.config.security.PrincipalDetails;
import com.social.bookshare.dto.request.LocationRegisterRequest;
import com.social.bookshare.service.LocationService;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

	private final LocationService locationService;
	
	public LocationController(LocationService locationService) {
		this.locationService = locationService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<String> registerLocation(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestBody LocationRegisterRequest request) {
		
		try {
			locationService.registerLocation(principalDetails.getId(), request);
			
			return ResponseEntity.ok(
					String.format(
							"Registered: %s (%d, %d)", // address (latitude, longitude)
							request.getAddress(), 
							request.getUserLat(), 
							request.getUserLon()
						)
					);
			
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	
//	public ResponseEntity() getUserLocations
}
