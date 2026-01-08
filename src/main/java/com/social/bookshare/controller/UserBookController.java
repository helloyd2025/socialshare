package com.social.bookshare.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.config.security.PrincipalDetails;
import com.social.bookshare.domain.Book;
import com.social.bookshare.domain.Location;
import com.social.bookshare.dto.request.BookRegisterRequest;
import com.social.bookshare.dto.response.BookSearchResult;
import com.social.bookshare.service.BookService;
import com.social.bookshare.service.LocationService;
import com.social.bookshare.service.UserBookService;

@RestController
@RequestMapping("/api/v1/user/books")
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
				locationId = locationService.registerLocation(principalDetails.getId(), null, address, userLat, userLon, true);
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
					locationId = locationService.registerLocation(userId, 
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
		    	userBookService.registerUserBook(userId, bookId, locationId, originalRequest.getComment(), originalRequest.getStatus());
		    	
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
}
