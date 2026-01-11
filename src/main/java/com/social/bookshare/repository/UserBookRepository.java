package com.social.bookshare.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	
	@Query(value = "SELECT COUNT(ub) > 0 FROM UserBook ub"
			+ " WHERE ub.location.id = :locationId"
			+ " AND ub.status IN (com.social.bookshare.domain.UserBook.Status.LOANED,"
			+ "                   com.social.bookshare.domain.UserBook.Status.PENDING_RETURN)")
	public boolean isLocationOccupied(@Param("locationId") Long locationId);
}
