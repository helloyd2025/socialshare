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
	
	private static final String AUDIENCE_AUTHENTICATION = "AUTHENTICATION";
	private static final String AUDIENCE_TFA_VERIFICATION = "TFA_VERIFICATION";
	
	public JwtTokenProvider(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
	
	@Value("${jwt.secret}")
	private String secretKey;
	
	@Value("${jwt.accessTokenValidTime}")
	private long accessTokenValidTime;
	
	@Value("${jwt.refreshTokenValidTime}")
	private long refreshTokenValidTime;
	
	@Value("${jwt.preAuthTokenValidTime}")
	private long preAuthTokenValidTime;
	
	private Key key;

    @PostConstruct
    protected void init() {
    	this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    public String createAccessToken(Long userId, String email, String role) {
    	return this.createToken(userId, email, role, accessTokenValidTime, AUDIENCE_AUTHENTICATION);
    }
    
    public String createRefreshToken(Long userId) {
        return this.createToken(userId, null, null, refreshTokenValidTime, null);
    }
    
    public String createPreAuthToken(Long userId, String email) {
    	return this.createToken(userId, email, null, preAuthTokenValidTime, AUDIENCE_TFA_VERIFICATION);
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
        return this.validateToken(token, AUDIENCE_AUTHENTICATION);
    }
    
    public boolean validateRefreshToken(String token) {
    	return this.validateToken(token, null);
    }

    public boolean validatePreAuthToken(String token) {
        return this.validateToken(token, AUDIENCE_TFA_VERIFICATION);
    }
    
    private boolean validateToken(String token, String expectedAudience) {
    	try {
    		Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    		// If expectedAudience is null, skip the audience claim check. Else, check for a match.
    		return expectedAudience == null || expectedAudience.equals(claims.getAudience());
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    private String createToken(Long userId, String userEmail, String role, long tokenValidTime, String audience) {
    	Claims claims = Jwts.claims().setSubject(userEmail);
    	claims.put("role", role);
    	claims.put("id", userId);
    	
    	if (audience != null) {
            claims.setAudience(audience);
        }
    	
    	Date now = new Date();
    	return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
