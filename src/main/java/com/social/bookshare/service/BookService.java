package com.social.bookshare.service;

import java.util.List;

import com.social.bookshare.domain.Book;
import com.social.bookshare.dto.request.BookRegisterRequest;
import com.social.bookshare.dto.response.BookLocationResponse;
import com.social.bookshare.dto.response.BookSearchResult;

public interface BookService {

	public List<BookSearchResult> searchLibraryBooks(String isbn13, String title, String author, String publisher, String keywords, int pageSize);
	public List<BookSearchResult> searchPrivateBooks(String isbn13, String title, Short volume, String className, String author, String publisher, boolean strictly);
	
	public List<BookLocationResponse> getIntegratedBookLocations(String isbn13, double userLat, double userLon, double refDist, int pageSize);
	
	public Book getBook(String isbn13);
	
	public Long registerBook(Long userId, BookRegisterRequest request);
//	public List<BookSearchResult> fetchFromApi(String title, String author, String isbn13, String keywords, String publisher, int pageSize);
}
