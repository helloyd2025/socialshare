package com.social.bookshare.config.security;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class PrincipalDetails extends User {

	private static final long serialVersionUID = 1L;
	
	private final Long id;
	
	public PrincipalDetails(com.social.bookshare.domain.User user) {
        super(
    		user.getEmail(), 
    		user.getPassword(), 
    		AuthorityUtils.createAuthorityList("ROLE_" + user.getRole().name())
    	);
        
        this.id = user.getId();
    }
	
	public Long getId() {
        return id;
    }
}
