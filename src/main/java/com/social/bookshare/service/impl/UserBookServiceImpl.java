package com.social.bookshare.service.impl;

import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.Book;
import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserBook;
import com.social.bookshare.domain.UserBook.Status;
import com.social.bookshare.dto.request.UserBookRegisterRequest;
import com.social.bookshare.dto.request.UserBookUpdateRequest;
import com.social.bookshare.repository.UserBookRepository;
import com.social.bookshare.service.UserBookService;
import com.social.bookshare.utils.EntityMapper;
import com.social.bookshare.utils.UserRoleUtils;

@Service
public class UserBookServiceImpl implements UserBookService {

	private final UserBookRepository userBookRepository;

	public UserBookServiceImpl(UserBookRepository userBookRepository) {
		this.userBookRepository = userBookRepository;
	}

	@Override
	@Transactional
	public Long registerUserBook(Long userId, UserBookRegisterRequest request) {
		UserBook ub = UserBook.builder()
				.user(EntityMapper.getReference(User.class, userId))
				.location(EntityMapper.getReference(Location.class, request.getLocationId()))
				.book(EntityMapper.getReference(Book.class, request.getBookId()))
				.comment(request.getComment())
				.status(request.getStatus())
				.build();
		
		return userBookRepository.save(ub).getId();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<UserBook> getUserBooks(Long userId) {
		return userBookRepository.findByUser(EntityMapper.getReference(User.class, userId));
	}

	@Override
	@Transactional
	public void updateUserBook(Long userId, UserBookUpdateRequest request) {
		UserBook userBook = userBookRepository.findById(request.getId())
				.orElseThrow(() -> new IllegalArgumentException("User Book not found"));
		
		if (userBook.getUserId() != userId || !UserRoleUtils.isUser())
			throw new BadCredentialsException("Illegal access: " + userId);
		
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
				.orElseThrow(() -> new IllegalArgumentException("User Book not found"));
		
		if (userBook.getUserId() != userId || !UserRoleUtils.isUser())
			throw new BadCredentialsException("Illegal access: " + userId);
		
		userBookRepository.delete(userBook);
	}
}
