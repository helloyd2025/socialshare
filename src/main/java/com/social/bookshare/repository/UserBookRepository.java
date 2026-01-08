package com.social.bookshare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.bookshare.domain.UserBook;
import com.social.bookshare.domain.UserBook.Status;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

	public List<UserBook> findByUserId(Long userId);
	public List<UserBook> findByLocationId(Long locationId);
	
	public List<UserBook> findByUserIdAndStatus(Long userId, Status status);
	public List<UserBook> findByLocationIdAndStatus(Long locationId, Status status);
}
