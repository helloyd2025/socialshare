package com.social.bookshare.service;

import com.social.bookshare.domain.UserBook;

public interface UserBookService {

	public UserBook registerUserBook(Long userId, Long bookId, Long locationId, String comment, String status);
}
