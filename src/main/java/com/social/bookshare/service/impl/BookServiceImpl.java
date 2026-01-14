package com.social.bookshare.service.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.social.bookshare.domain.Book;
import com.social.bookshare.domain.Book.Kdc;
import com.social.bookshare.dto.request.BookRegisterRequest;
import com.social.bookshare.dto.response.BookLocationResponse;
import com.social.bookshare.dto.response.BookSearchResponse;
import com.social.bookshare.dto.response.BookSearchResult;
import com.social.bookshare.dto.response.LibraryByBookResponse;
import com.social.bookshare.repository.BookRepository;
import com.social.bookshare.repository.BookRepository.BookLocationProjection;
import com.social.bookshare.service.BookService;
import com.social.bookshare.utils.BookUtils;
import com.social.bookshare.utils.GeometryUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;
    private final RestTemplate restTemplate;
    
    public BookServiceImpl(BookRepository bookRepository, RestTemplate restTemplate) {
    	this.bookRepository = bookRepository;
    	this.restTemplate = restTemplate;
    }

    @Value("${data4library.api.url}")
    private String apiUrl;

    @Value("${data4library.api.key}")
    private String authKey;
    
    @Override
    public List<BookSearchResult> searchLibraryBooks(String isbn13, String title, String author, String publisher, String keywords, int pageSize) {
    	URI uri = UriComponentsBuilder.fromUriString(apiUrl + "/srchBooks")
                .queryParam("authKey", authKey)
                .queryParam("title", Optional.ofNullable(title))
                .queryParam("author", Optional.ofNullable(author))
                .queryParam("isbn13", Optional.ofNullable(isbn13))
                .queryParam("keyword", Optional.ofNullable(keywords)) // Trust conversion on client
                .queryParam("publisher", Optional.ofNullable(publisher))
                .queryParam("pageSize", pageSize)
                .queryParam("format", "json")
                .build(true).toUri();

        BookSearchResponse response = restTemplate.getForObject(uri, BookSearchResponse.class);

        if (response == null || response.getResponse() == null || response.getResponse().getDocs() == null) {
            return Collections.emptyList();
        }

        return response.getResponse().getDocs().stream()
                .<BookSearchResult>map(item -> {
                    BookSearchResponse.DocInfo doc = item.getDoc();
                    return BookSearchResult.builder()
                            .title(doc.getBookName())
                            .volume(doc.getVolume())
                            .className(doc.getClassName())
                            .author(doc.getAuthors())
                            .publisher(doc.getPublisher())
                            .isbn13(doc.getIsbn13())
                            .image(doc.getBookImageURL())
                            .fromLibrary(true)
                            .build();
                }).collect(Collectors.toList());
    }

	@Override
	@Transactional(readOnly = true)
	public List<BookSearchResult> searchPrivateBooks(String isbn13, String title, Short volume, String className, String author, String publisher, boolean strictly) {
		if (isbn13 == null) {
			if (volume == null) { volume = 1; }
			// This is for informal books. But not matter, even if book has formal ISBN...
			isbn13 = BookUtils.generateBookHash(title, volume, author, publisher);
		}
		
		if (strictly) {
			return bookRepository.searchPrivateBooksStrictly(isbn13, title, volume, Kdc.valueOf(className.toUpperCase()), author, publisher);
		} else {
			return bookRepository.searchPrivateBooks(isbn13, title, volume, Kdc.valueOf(className.toUpperCase()), author, publisher);
		}
	}
    
    @Override
    @Transactional(readOnly = true)
	public List<BookLocationResponse> getIntegratedBookLocations(String isbn13, double userLat, double userLon, double refDist, int pageSize) {
    	if (pageSize > 20) {
    		throw new IllegalArgumentException("Page size: too many to load");
    	}
    	List<BookLocationResponse> privateResponses = new ArrayList<>();
    	List<BookLocationResponse> libraryResponses = new ArrayList<>();

        Point userLocation = GeometryUtils.createPoint(userLon, userLat);
        
        // 1. DB private books (PostGIS)
        List<BookLocationProjection> dbResults = bookRepository.findPrivateBookLocationsWithDistance(isbn13, userLocation, pageSize/2);
        
        for (BookLocationProjection dbItem : dbResults) {
        	double distance = dbItem.getDistance();
        	
        	privateResponses.add(BookLocationResponse.builder()
        			.type("PRIVATE")
        			.label(dbItem.getLabel())
        			.address(dbItem.getAddress())
        			.distance(Math.round(distance * 100.0) / 100.0)
        			.lat(dbItem.getLat())
        			.lon(dbItem.getLon())
        			.isHighlight(distance <= refDist)
        			.build());
        }
        
        // 2. API libraries
        List<LibraryByBookResponse.LibInfo> apiLibraries = this.findLibrariesByIsbn(isbn13, pageSize-privateResponses.size());
        
        for (LibraryByBookResponse.LibInfo lib : apiLibraries) {
        	double libLat = Double.parseDouble(lib.getLatitude());
            double libLon = Double.parseDouble(lib.getLongitude());
            
            double distance = GeometryUtils.calculateDistance(libLon, libLat, userLon, userLat);
            
            libraryResponses.add(BookLocationResponse.builder()
            		.type("LIBRARY")
            		.label(lib.getLibName())
            		.address(lib.getAddress())
            		.distance(Math.round(distance * 100.0) / 100.0)
            		.lat(libLon)
            		.lon(libLat)
            		.isHighlight(distance <= refDist)
            		.build());
        }
        
        privateResponses.sort(BookLocationResponse.DISTANCE_COMPARATOR);
        libraryResponses.sort(BookLocationResponse.DISTANCE_COMPARATOR);
        
        return Stream.concat(privateResponses.stream(), libraryResponses.stream())
        		.collect(Collectors.toList());
	}
    
    @Override
    @Transactional(readOnly = true)
    public Book getBook(String isbn13) {
    	return bookRepository.findByIsbn13(isbn13)
    			.orElseThrow(() -> new EntityNotFoundException("Book not found"));
    }
    
    @Override
    @Transactional
    public Book registerBook(BookRegisterRequest request) {
    	if (request.getIsbn13() == null) {
			String fakeIsbn13 = BookUtils.generateBookHash(request.getTitle(), request.getVolume(), request.getAuthor(), request.getPublisher());
			request.setIsbn13(fakeIsbn13);
		}
		
		Book book = Book.builder()
				.isbn13(request.getIsbn13())
				.title(request.getTitle())
				.volume(request.getVolume())
				.className(request.getClassName())
				.author(request.getAuthor())
				.publisher(request.getPublisher())
				.imageURL(request.getImageUrl())
				.build();
		
		return bookRepository.save(book);
    }
    
    private List<LibraryByBookResponse.LibInfo> findLibrariesByIsbn(String isbn13, int pageSize) {
        URI uri = UriComponentsBuilder.fromUriString(apiUrl + "/libSrchByBook")
                .queryParam("authKey", authKey)
                .queryParam("isbn13", isbn13)
                .queryParam("pageSize", pageSize)
                .queryParam("format", "json")
                .build(true)
                .toUri();

        LibraryByBookResponse response = restTemplate.getForObject(uri, LibraryByBookResponse.class);

        if (response == null || response.getResponse() == null || response.getResponse().getLibs() == null) {
            return Collections.emptyList();
        }

        return response.getResponse().getLibs().stream()
                .map(LibraryByBookResponse.Lib::getLib)
                .collect(Collectors.toList());
    }
}
