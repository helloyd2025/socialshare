package com.social.bookshare.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserKey;

public interface UserKeyRepository extends JpaRepository<UserKey, Long> {

	public List<UserKey> findByUser(User user);
	public List<UserKey> findByUserAndType(User user, String type);
}