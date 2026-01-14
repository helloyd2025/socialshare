package com.social.bookshare.config.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {
	
	private final UserDetailsService userDetailsService;
	
	public JwtTokenProvider(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
	
	@Value("${jwt.secret}")
	private String secretKey;
	
	@Value("${jwt.accessTokenValidTime}")
	private long accessTokenValidTime;
	
	@Value("${jwt.refreshTokenValidTime}")
	private long refreshTokenValidTime;
	
	private Key key;

    @PostConstruct
    protected void init() {
    	this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    public String createAccessToken(Long userId, String email, String role) {
    	return createToken(userId, email, role, accessTokenValidTime);
    }
    
    public String createRefreshToken(Long userId) {
        return createToken(userId, null, null, refreshTokenValidTime);
    }
    
    public Long getUserId(String token) {
    	return Jwts.parserBuilder()
    			.setSigningKey(key)
    			.build()
    			.parseClaimsJws(token)
    			.getBody()
                .get("id", Long.class);
    }
    
    public Authentication getAuthentication(String token) {
    	String email = Jwts.parserBuilder()
    			.setSigningKey(key)
    			.build()
    			.parseClaimsJws(token)
    			.getBody()
    			.getSubject();
    	
    	UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    	
    	return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    
    public boolean validateToken(String token) {
    	try {
    		Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    		return true;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    private String createToken(Long userId, String userEmail, String role, long tokenValidTime) {
    	Claims claims = Jwts.claims().setSubject(userEmail);
    	claims.put("role", role);
    	claims.put("id", userId);
    	
    	Date now = new Date();
    	
    	return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
