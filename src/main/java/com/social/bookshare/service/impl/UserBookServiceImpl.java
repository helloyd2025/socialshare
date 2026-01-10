package com.social.bookshare.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.Book;
import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserBook;
import com.social.bookshare.domain.UserBook.Status;
import com.social.bookshare.dto.request.BookRegisterRequest;
import com.social.bookshare.dto.request.UserBookRegisterRequest;
import com.social.bookshare.dto.request.UserBookUpdateRequest;
import com.social.bookshare.dto.response.BookSearchResult;
import com.social.bookshare.dto.response.UserBookResponse;
import com.social.bookshare.repository.UserBookRepository;
import com.social.bookshare.service.BookService;
import com.social.bookshare.service.LocationService;
import com.social.bookshare.service.UserBookService;
import com.social.bookshare.utils.EntityMapper;
import com.social.bookshare.utils.UserRoleUtils;

import io.netty.handler.timeout.ReadTimeoutException;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserBookServiceImpl implements UserBookService {

	private final UserBookRepository userBookRepository;
	private final BookService bookService;
	private final LocationService locationService;
	private final RedissonClient redissonClient;

	public UserBookServiceImpl(UserBookRepository userBookRepository, BookService bookService,
			LocationService locationService, RedissonClient redissonClient) {
		this.userBookRepository = userBookRepository;
		this.bookService = bookService;
		this.locationService = locationService;
		this.redissonClient = redissonClient;
	}

	@Override
	@Transactional
	public BookSearchResult requestBookRegistration(Long userId, BookRegisterRequest request) {
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
		boolean hasDetails = Stream.of(title, className, author, publisher).allMatch(s -> s != null && !s.isBlank());
		// request must have either lon-lat pair or label
		boolean hasLabel = label != null && !label.isBlank();
		boolean hasLatLon = Stream.of(userLat, userLon).allMatch(d -> d != null);
		
		if (!((hasIsbn && !hasDetails) || (!hasIsbn && hasDetails)) 
			|| !((hasLabel && !hasLatLon) || (!hasLabel && hasLatLon))) {
			throw new IllegalArgumentException("Invalid register request");
		}
		
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
				throw new EntityNotFoundException("Book Not Found");
			} else 
				suggestedBook = new BookSearchResult(title, volume, className, author, publisher, isbn13, request.getImageUrl(), true);
		}
		request.setBookSearchResult(suggestedBook);
		
		// 2. User_locations
		if (label != null) { // Register with saved location
			locationId = locationService.getUserLocation(userId, label).getId();
		} else { // Register with current location
			locationId = locationService.registerUserLocation(userId, null, address, userLat, userLon, true).getId();
		}
		request.setLocationId(locationId);
		
		RBucket<BookRegisterRequest> pendingBucket = redissonClient.getBucket("PENDING_REG:" + userId);
        pendingBucket.set(request, Duration.ofMinutes(10)); // User must confirm within 10 minutes
        
        return suggestedBook;
	}

	@Override
	@Transactional
	public Map<String, String> confirmBookRegistration(Long userId, boolean confirm) {
	    RBucket<BookRegisterRequest> pendingBucket = redissonClient.getBucket("PENDING_REG:" + userId);
	    BookRegisterRequest originalRequest = pendingBucket.get();
	    
	    if (originalRequest == null) {
	    	// To-do : throw new TimeoutException()
	        throw new ReadTimeoutException();
	    }
	    
	    try {
	    	Map<String, String> response = new HashMap<>();
		    
		    if (confirm) {
		    	Long bookId;
		    	Long locationId;
		    	
				if (originalRequest.isFirstBook()) {
					// books
					bookId = bookService.registerBook(userId, originalRequest).getId();
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
							true).getId();
					
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
				
		    	this.registerUserBook(userId, ubRequest);
		    	
	    		response.put("title", originalRequest.getTitle());
	    		response.put("firstBook", originalRequest.isFirstBook().toString());
		    	
		        return response;
		    } else {
		        return null;
		    }
	    } finally {
	    	pendingBucket.delete();
	    }
	}

	@Override
	@Transactional
	public UserBook registerUserBook(Long userId, UserBookRegisterRequest request) {
		UserBook ub = UserBook.builder()
				.owner(EntityMapper.getReference(User.class, userId))
				.location(EntityMapper.getReference(Location.class, request.getLocationId()))
				.book(EntityMapper.getReference(Book.class, request.getBookId()))
				.comment(request.getComment())
				.status(request.getStatus())
				.build();
		
		return userBookRepository.save(ub);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UserBookResponse> getUserBooks(Long userId) {
		return userBookRepository.findByUser(EntityMapper.getReference(User.class, userId)).stream()
				.<UserBookResponse>map(ub -> UserBookResponse.builder()
						.id(ub.getId())
						.location(ub.getLocation())
						.book(ub.getBook())
						.comment(ub.getComment())
						.loaner(ub.getLoaner())
						.status(ub.getStatus())
						.updatedAt(ub.getUpdatedAt())
						.build())
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public void updateUserBook(Long userId, UserBookUpdateRequest request) {
		UserBook userBook = userBookRepository.findById(request.getId())
				.orElseThrow(() -> new EntityNotFoundException("User Book not found"));
		
		if (userBook.getOwnerId() != userId || !UserRoleUtils.isUser()) { // Credential check
			throw new AccessDeniedException("Illegal access: " + userId);
		} else if (!userBook.isNotLoaned()) { // Loan status check
			throw new IllegalStateException("A book info on loan cannot be changed.");
		}
		
		if (request.getLocationId() == null) {
			// Register location
			Long locationId = locationService.registerUserLocation(userId, 
					null, request.getAddress(), request.getUserLat(), request.getUserLon(), true).getId();
			request.setLocationId(locationId);
		}
		
		userBook.updateUserBook(
				EntityMapper.getReference(Location.class, request.getLocationId()), 
				request.getComment(), 
				Status.valueOf(request.getStatus().strip().toUpperCase())
			);
	}

	@Override
	@Transactional
	public void deleteUserBook(Long userId, Long userBookId) {
		UserBook userBook = userBookRepository.findById(userBookId)
				.orElseThrow(() -> new EntityNotFoundException("User Book not found"));
		
		if (userBook.getOwnerId() != userId || !UserRoleUtils.isUser()) { // Credential check
			throw new AccessDeniedException("Illegal access: " + userId);
		} else if (!userBook.isNotLoaned()) { // Loan status check
			throw new IllegalStateException("A book info on loan cannot be changed.");
		}
		
		userBookRepository.delete(userBook);
	}
}
