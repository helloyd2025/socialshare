package com.social.bookshare.repository;

import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.bookshare.domain.Location;
import com.social.bookshare.domain.User;

public interface LocationRepository extends JpaRepository<Location, Long> {
	
	public List<Location> findByUser(User user);
	
	public Optional<Location> findById(Long locationId);
	public Optional<Location> findByUserAndLabel(User user, String label);
	public Optional<Location> findByUserAndLocation(User user, Point location);
	
	public boolean existsByUserIdAndAddress(User user, String address);
	
	@Query(value = "SELECT * FROM user_locations l " +
            "WHERE ST_DWithin(l.location::geography, :userPoint::geography, :distance) " +
            "AND l.is_active = true", 
            nativeQuery = true)
	List<Location> findNearbyLocations(@Param("userPoint") Point userPoint, @Param("distance") double distance);
}
