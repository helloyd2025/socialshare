package com.social.bookshare.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserRoleUtils {

	public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null) 
        	return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(grantedRole -> grantedRole.equals(role.toUpperCase()));
    }

    public static boolean isUser() {
        return hasRole("ROLE_USER");
    }
    
    public static boolean isAdmin() {
    	return hasRole("ROLE_ADMIN");
    }
}
