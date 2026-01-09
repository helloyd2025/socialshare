package com.social.bookshare.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
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
import com.social.bookshare.dto.request.UserBookRegisterRequest;
import com.social.bookshare.dto.request.UserBookUpdateRequest;
import com.social.bookshare.dto.response.BookSearchResult;
import com.social.bookshare.dto.response.UserBookResponse;
import com.social.bookshare.service.BookService;
import com.social.bookshare.service.LocationService;
import com.social.bookshare.service.UserBookService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/user-books")
public class UserBookController {
	
	private final BookService bookService;
	private final LocationService locationService;
	private final UserBookService userBookService;
	private final RedissonClient redissonClient;
	
	public UserBookController(BookService bookService, LocationService locationService, UserBookService userBookService, RedissonClient redissonClient) {
		this.bookService = bookService;
		this.locationService = locationService;
		this.userBookService = userBookService;
		this.redissonClient = redissonClient;
	}

	@PostMapping("/register/request")
	public ResponseEntity<BookSearchResult> requestRegistration(
			@AuthenticationPrincipal PrincipalDetails principalDetails, 
			@RequestBody BookRegisterRequest request) {

		String isbn13 = request.getIsbn13();
		String title = request.getTitle();
		Short volume = request.getVolume();
		String className = request.getClassName();
		String author = request.getAuthor();
		String publisher = request.getPublisher();
		String label = request.getLabel();
		String address = request.getAddress();
		Double userLat = request.getUserLat();
		Double userLon = request.getUserLon();

		// request must have either ISBN only or others all
		boolean hasIsbn = isbn13 != null && !isbn13.isBlank();
		boolean hasOtherDetails = Stream.of(title, className, author, publisher).allMatch(s -> s != null && !s.isBlank());
		// request must have either lon-lat pair or label
		boolean hasLabel = label != null && !label.isBlank();
		boolean hasLatLon = Stream.of(userLat, userLon).allMatch(d -> d != null);
		
		if (!((hasIsbn && !hasOtherDetails) || (!hasIsbn && hasOtherDetails)) 
			|| !((hasLabel && !hasLatLon) || (!hasLabel && hasLatLon))) {
			return ResponseEntity.badRequest().build();
		}

		try {
			BookSearchResult suggestedBook;
			Long locationId;
			
			// 1. Books (DB private before API libraries)
			List<BookSearchResult> searchResults = bookService.searchPrivateBooks(isbn13, title, volume, className, author, publisher, true);
			
			if (isbn13 != null && searchResults.isEmpty()) {
				searchResults = bookService.searchLibraryBooks(isbn13, null, null, null, null, 1);
			}

			if (!searchResults.isEmpty()) {
				suggestedBook = searchResults.get(0);
			} else {
				if (isbn13 != null) {
					return ResponseEntity.notFound().build();
				}
				suggestedBook = new BookSearchResult(title, volume, className, author, publisher, isbn13, request.getImageUrl(), true);
			}
			request.setBookSearchResult(suggestedBook);
			
			// 2. User_locations
			if (label != null) { // Register with saved location
				locationId = locationService.getUserLocation(principalDetails.getId(), label).getId();
			} else { // Register with current location
				locationId = locationService.registerUserLocation(principalDetails.getId(), null, address, userLat, userLon, true);
			}
			request.setLocationId(locationId);
			
			RBucket<BookRegisterRequest> pendingBucket = redissonClient.getBucket("PENDING_REG:" + principalDetails.getId());
            pendingBucket.set(request, Duration.ofMinutes(10)); // User must confirm within 10 minutes
            
            return ResponseEntity.ok(suggestedBook);
			
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@PostMapping("/register/confirm")
	public ResponseEntity<Map<String, String>> confirmRegistration(
	        @AuthenticationPrincipal PrincipalDetails principalDetails,
	        @RequestParam boolean confirm) {
		
		Long userId = principalDetails.getId();
	    RBucket<BookRegisterRequest> pendingBucket = redissonClient.getBucket("PENDING_REG:" + userId);
	    BookRegisterRequest originalRequest = pendingBucket.get();
	    
	    if (originalRequest == null) 
	        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
	    
	    try {
	    	Map<String, String> response = new HashMap<>();
		    
		    if (confirm) {
		    	Long bookId;
		    	Long locationId;
		    	
				if (originalRequest.getIsFirstBook()) {
					// books
					bookId = bookService.registerBook(userId, originalRequest);
					originalRequest.setBookId(bookId);
				} else {
					bookId = bookService.getBook(originalRequest.getIsbn13()).getId();
				}
				
				if (originalRequest.getLabel() == null) {
					// user_locations
					locationId = locationService.registerUserLocation(userId, 
							originalRequest.getLabel(), 
							originalRequest.getAddress(), 
							originalRequest.getUserLat(), 
							originalRequest.getUserLon(), 
							true);
					
					originalRequest.setLocationId(locationId);
				} else {
					locationId = locationService.getUserLocation(userId, originalRequest.getLabel()).getId();
					originalRequest.setLocationId(locationId);
				}
				
				// user_books
				UserBookRegisterRequest ubRequest = UserBookRegisterRequest.builder()
						.locationId(locationId)
						.bookId(bookId)
						.comment(originalRequest.getComment())
						.status(originalRequest.getStatus())
						.build();
				
		    	userBookService.registerUserBook(userId, ubRequest);
		    	
	    		response.put("title", originalRequest.getTitle());
	    		response.put("firstBook", originalRequest.getIsFirstBook().toString());
		    	
		        return ResponseEntity.ok(response);
		    } else {
		        return ResponseEntity.ok(null);
		    }
	    } catch (Exception e) {
	    	return ResponseEntity.internalServerError().build();
	    } finally {
	    	pendingBucket.delete();
	    }
	}
	
	@GetMapping("/inventory")
	public ResponseEntity<List<UserBookResponse>> getUserBooks(@AuthenticationPrincipal PrincipalDetails principalDetails) {
		List<UserBookResponse> userBooks = userBookService.getUserBooks(principalDetails.getId()).stream()
				.<UserBookResponse>map(ub -> UserBookResponse.builder()
						.id(ub.getId())
						.location(ub.getLocation())
						.book(ub.getBook())
						.comment(ub.getComment())
						.status(ub.getStatus())
						.updatedAt(ub.getUpdatedAt())
						.build())
				.collect(Collectors.toList());
		
		return ResponseEntity.ok(userBooks);
	}
	
	@PatchMapping("/inventory/update")
	public ResponseEntity<String> updateUserBook(
			@AuthenticationPrincipal PrincipalDetails principalDetails,
			@RequestBody UserBookUpdateRequest request) {
		try {
			if (request.getLocationId() == null) {
				// Register location
				Long locationId = locationService.registerUserLocation(principalDetails.getId(), 
						null, request.getAddress(), request.getUserLat(), request.getUserLon(), true);
				request.setLocationId(locationId);
			}
			
			userBookService.updateUserBook(principalDetails.getId(), request);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
			
		} catch (BadCredentialsException | IllegalArgumentException e) {
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
		} catch (BadCredentialsException | IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
