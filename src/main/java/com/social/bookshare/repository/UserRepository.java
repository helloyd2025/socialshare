package com.social.bookshare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.bookshare.domain.User;

public interface UserRepository extends JpaRepository<User, Long>{

	public Optional<User> findByEmail(String email);
	
	public boolean existsById(Long id);
	public boolean existsByEmail(String email);
	public boolean existsByName(String name);
}
