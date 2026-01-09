package com.social.bookshare.service;

import java.util.List;

import com.social.bookshare.domain.UserBook;
import com.social.bookshare.dto.request.UserBookRegisterRequest;
import com.social.bookshare.dto.request.UserBookUpdateRequest;

public interface UserBookService {

	public Long registerUserBook(Long userId, UserBookRegisterRequest request);
	
	public List<UserBook> getUserBooks(Long userId);
	
	public void updateUserBook(Long userId, UserBookUpdateRequest request);
	public void deleteUserBook(Long userId, Long userBookId);
}
