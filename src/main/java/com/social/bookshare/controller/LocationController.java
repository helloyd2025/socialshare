package com.social.bookshare.controller;

import java.net.URI;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.social.bookshare.config.security.PrincipalDetails;
import com.social.bookshare.domain.Location;
import com.social.bookshare.dto.request.LocationRegisterRequest;
import com.social.bookshare.dto.request.LocationUpdateRequest;
import com.social.bookshare.dto.response.UserLocationReponse;
import com.social.bookshare.service.LocationService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

	private final LocationService locationService;
	
	public LocationController(LocationService locationService) {
		this.locationService = locationService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<UserLocationReponse> registerLocation(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestBody LocationRegisterRequest request) {
		
		try {
			Location registeredLocation = locationService.registerUserLocation(principalDetails.getId(), request);
            
            UserLocationReponse responseDto = UserLocationReponse.builder()
                    .id(registeredLocation.getId())
                    .label(registeredLocation.getLabel())
                    .address(registeredLocation.getAddress())
                    .location(registeredLocation.getLocation())
                    .isActive(registeredLocation.isActive())
                    .createdAt(registeredLocation.getCreatedAt())
                    .build();
            
            URI locationUri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(registeredLocation.getId())
                    .toUri();
			
			return ResponseEntity.created(locationUri).body(responseDto);
			
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@GetMapping("/user/inventory")
	public ResponseEntity<List<UserLocationReponse>> getUserLocations(@AuthenticationPrincipal PrincipalDetails principalDetails) {
		return ResponseEntity.ok(locationService.getUserLocations(principalDetails.getId()));
	}
	
	@PatchMapping("/user/inventory/update")
	public ResponseEntity<String> updateUserLocation(
			@AuthenticationPrincipal PrincipalDetails principalDetails, 
			@RequestBody LocationUpdateRequest request) {
		try {
			locationService.updateUserLocation(principalDetails.getId(), request);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (BadCredentialsException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@DeleteMapping("/user/inventory/{locationId}/delete")
	public ResponseEntity<String> deleteUserLocation(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@PathVariable @NotBlank Long locationId) {
		try {
			locationService.deleteUserLocation(principalDetails.getId(), locationId);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (BadCredentialsException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
