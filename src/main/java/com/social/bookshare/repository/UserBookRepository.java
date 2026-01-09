package com.social.bookshare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserBook;
import com.social.bookshare.domain.UserBook.Status;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

	public Optional<UserBook> findById(Long id);
	
	public List<UserBook> findByUser(User user);
	public List<UserBook> findByLocation(Location location);
	
	public List<UserBook> findByUserAndStatus(User user, Status status);
	public List<UserBook> findByLocationAndStatus(Location location, Status status);
}
