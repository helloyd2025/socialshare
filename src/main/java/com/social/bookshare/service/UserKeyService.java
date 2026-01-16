package com.social.bookshare.service;

import com.social.bookshare.domain.UserKey;
import com.social.bookshare.dto.request.UserKeyRegisterRequest;

public interface UserKeyService {

	public UserKey registerKey(Long userId, UserKeyRegisterRequest request) throws Exception;
}
