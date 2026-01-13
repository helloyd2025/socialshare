package com.social.bookshare.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.config.security.PrincipalDetails;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.PassUpdateRequest;
import com.social.bookshare.service.UserService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final UserService userService;
	
	public AuthController(UserService userService) {
		this.userService = userService;
	}
	
	@PatchMapping("/pass/update")
	public ResponseEntity<Void> updateUserPassword(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestBody PassUpdateRequest request) {
		try {
			userService.updatePassword(principalDetails.getId(), Role.USER, request);
			return ResponseEntity.status(HttpStatus.ACCEPTED).build();
			
		} catch (EntityNotFoundException | AccessDeniedException | BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
