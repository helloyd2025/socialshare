package com.social.bookshare.service.impl;

import org.springframework.stereotype.Service;

import com.social.bookshare.domain.Book;
import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserBook;
import com.social.bookshare.repository.UserBookRepository;
import com.social.bookshare.service.UserBookService;
import com.social.bookshare.utils.EntityMapper;

import jakarta.transaction.Transactional;

@Service
public class UserBookServiceImpl implements UserBookService {

	private final UserBookRepository userBookRepository;
	private final EntityMapper entityMapper;

	public UserBookServiceImpl(UserBookRepository userBookRepository, EntityMapper entityMapper) {
		this.userBookRepository = userBookRepository;
		this.entityMapper = entityMapper;
	}

	@Override
	@Transactional
	public UserBook registerUserBook(Long userId, Long bookId, Long locationId, String comment, String status) {
		UserBook ub = UserBook.builder()
				.user(entityMapper.getReference(User.class, userId))
				.location(entityMapper.getReference(Location.class, locationId))
				.book(entityMapper.getReference(Book.class, bookId))
				.comment(comment)
				.status(status)
				.build();
		
		return userBookRepository.save(ub);
	}
}
