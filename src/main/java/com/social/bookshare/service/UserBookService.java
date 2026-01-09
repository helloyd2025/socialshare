package com.social.bookshare.service;

import java.util.List;
import java.util.Map;

import com.social.bookshare.domain.UserBook;
import com.social.bookshare.dto.request.BookRegisterRequest;
import com.social.bookshare.dto.request.UserBookRegisterRequest;
import com.social.bookshare.dto.request.UserBookUpdateRequest;
import com.social.bookshare.dto.response.BookSearchResult;
import com.social.bookshare.dto.response.UserBookResponse;

public interface UserBookService {

	public BookSearchResult requestBookRegistration(Long userId, BookRegisterRequest request);
	public Map<String, String> confirmBookRegistration(Long userId, boolean confirm);
	
	public UserBook registerUserBook(Long userId, UserBookRegisterRequest request);
	
	public List<UserBookResponse> getUserBooks(Long userId);
	
	public void updateUserBook(Long userId, UserBookUpdateRequest request);
	public void deleteUserBook(Long userId, Long userBookId);
}
