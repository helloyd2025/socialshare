package com.social.bookshare.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.dto.response.BookLocationResponse;
import com.social.bookshare.dto.response.BookSearchResult;
import com.social.bookshare.service.BookService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

	private final BookService bookService;
	
	public BookController(BookService bookService) {
		this.bookService = bookService;
	}
	
	@GetMapping("/search")
    public ResponseEntity<List<BookSearchResult>> searchBooks(
    		@RequestParam(required = false) String isbn13,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Short volume, // Users may not know volume. Stay null even if absent, when searching.
            @RequestParam(value = "class_nm", required = false) String className,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String keywords,
            @RequestParam(value = "page_size", defaultValue = "20") int pageSize) {
		
		if (Stream.of(isbn13, title, author, publisher, keywords).allMatch(s -> s == null || s.isBlank())) {
            return ResponseEntity.badRequest().build();
        }

		// 1. Search libraries (data4library.kr)
        List<BookSearchResult> apiResults = bookService.searchLibraryBooks(isbn13, title, author, publisher, keywords, pageSize);
        
        if (apiResults.size() >= pageSize) {
        	return ResponseEntity.ok(apiResults);
        }
        
        // If results less than pageSize...
    	Map<String, BookSearchResult> integratedMap = new HashMap<>(); // Collector for except duplicates
    	
    	for (BookSearchResult apiItem : apiResults) {
            integratedMap.put(apiItem.getIsbn13(), apiItem);
        }
    	
    	// 2. Search private books (DB)
    	List<BookSearchResult> privateResults = bookService.searchPrivateBooks(isbn13, title, volume, className, author, publisher, false);
    	
    	for (BookSearchResult privateItem : privateResults) {
    		if (integratedMap.size() >= pageSize) { break; }
        	
            if (!integratedMap.containsKey(privateItem.getIsbn13())) {
                integratedMap.put(privateItem.getIsbn13(), privateItem);
            }
        }
        
        if (integratedMap.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
        	return ResponseEntity.ok(new ArrayList<>(integratedMap.values()));
        }
	}
	
	@GetMapping("/{isbn13}/locations")
    public ResponseEntity<List<BookLocationResponse>> getIntegratedBookLocations(
            @PathVariable @NotBlank String isbn13,
            @RequestParam(value = "user_lat") double userLat,
            @RequestParam(value = "user_lon") double userLon,
            @RequestParam(value = "ref_dist", defaultValue = "1.0") double refDist,
            @RequestParam(value = "page_size", defaultValue = "5") int pageSize) {
        try {
            return ResponseEntity.ok(bookService.getIntegratedBookLocations(isbn13, userLat, userLon, refDist, pageSize));
        } catch (IllegalArgumentException e) {
        	return ResponseEntity.badRequest().build();
        } catch (Exception e) {
        	return ResponseEntity.internalServerError().build();
        }
    }
}
