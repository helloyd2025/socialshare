package com.social.bookshare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.bookshare.domain.User;

public interface UserRepository extends JpaRepository<User, Long>{

	public Optional<User> findByEmail(String email);
	
	public boolean existsById(Long id);
	
	@Query(value = "SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
	public boolean existsByEmail(@Param("email") String email);
	
	@Query(value = "SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.name) = LOWER(:name)")
	public boolean existsByName(@Param("name") String name);
}
