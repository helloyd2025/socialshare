package com.social.bookshare.controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.config.security.PrincipalDetails;
import com.social.bookshare.dto.request.BookRegisterRequest;
import com.social.bookshare.dto.request.UserBookUpdateRequest;
import com.social.bookshare.dto.response.BookSearchResult;
import com.social.bookshare.dto.response.UserBookResponse;
import com.social.bookshare.service.UserBookService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/user-books")
public class UserBookController {
	
	private final UserBookService userBookService;
	
	public UserBookController(UserBookService userBookService) {
		this.userBookService = userBookService;
	}

	@PostMapping("/register/request")
	public ResponseEntity<BookSearchResult> requestRegistration(
			@AuthenticationPrincipal PrincipalDetails principalDetails, 
			@RequestBody BookRegisterRequest request) {
		try {
            return ResponseEntity.ok(userBookService.requestBookRegistration(principalDetails.getId(), request));
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(null);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@PostMapping("/register/confirm")
	public ResponseEntity<Map<String, String>> confirmRegistration(
	        @AuthenticationPrincipal PrincipalDetails principalDetails,
	        @RequestParam boolean confirm) {
	    try {
	        return ResponseEntity.ok(userBookService.confirmBookRegistration(principalDetails.getId(), confirm));
		} catch (IllegalArgumentException e) {
			// In service layer, "Time-out" error will be thrown.
			if(e.getMessage().contains("Time-out")) {
		        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
			} else 
				return ResponseEntity.badRequest().build();
	    } catch (Exception e) {
	    	return ResponseEntity.internalServerError().build();
	    }
	}
	
	@GetMapping("/inventory")
	public ResponseEntity<List<UserBookResponse>> getUserBooks(@AuthenticationPrincipal PrincipalDetails principalDetails) {
		return ResponseEntity.ok(userBookService.getUserBooks(principalDetails.getId()));
	}
	
	@PatchMapping("/inventory/update")
	public ResponseEntity<String> updateUserBook(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestBody UserBookUpdateRequest request) {
		try {
			userBookService.updateUserBook(principalDetails.getId(), request);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (BadCredentialsException | EntityNotFoundException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@DeleteMapping("/inventory/{userBookId}/delete")
	public ResponseEntity<String> deleteUserBook(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@PathVariable @NotBlank Long userBookId) {
		try {
			userBookService.deleteUserBook(principalDetails.getId(), userBookId);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (BadCredentialsException | EntityNotFoundException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
