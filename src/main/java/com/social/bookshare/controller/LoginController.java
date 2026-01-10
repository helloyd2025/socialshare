package com.social.bookshare.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.AuthenticateRequest;
import com.social.bookshare.dto.request.TwoFactorAuthRequest;
import com.social.bookshare.dto.response.TokenResponse;
import com.social.bookshare.service.AuthService;
import com.social.bookshare.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/user")
public class LoginController {

	private final UserService userService;
	private final AuthService authService;

    public LoginController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }
    
    @Value("${jwt.refreshTokenValidTime}")
	private long refreshTokenValidTime;
    
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(HttpServletResponse response, @RequestBody AuthenticateRequest request) {
    	try {
    		User user = userService.authenticate(request, Role.USER);
    		TokenResponse tokenResponse = authService.issueTokensForPlainAuth(user);
    		
    		if (tokenResponse.requiresTwoFactor()) {
    			return ResponseEntity.ok(tokenResponse);
    		} else {
    			this.setSecureCookie(tokenResponse.getRefreshToken(), response);
                return ResponseEntity.ok(tokenResponse.toPublicResponse());
    		}
    	} catch (UsernameNotFoundException | AccessDeniedException | BadCredentialsException e) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/2fa/authenticate")
    public ResponseEntity<TokenResponse> loginWith2FA(@RequestBody TwoFactorAuthRequest request, HttpServletResponse response) {
        try {
            TokenResponse tokenResponse = authService.issueTokensForTwoFactorAuth(request);
            
            this.setSecureCookie(tokenResponse.getRefreshToken(), response);
            return ResponseEntity.ok(tokenResponse.toPublicResponse());
            
        } catch (UsernameNotFoundException | AccessDeniedException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(HttpServletResponse response, @RequestBody AuthenticateRequest request) {
    	try {
    		User user = userService.signup(request, Role.USER);
    		// As 2FA is disabled immediately after signing up, tokens will be issued.
    		TokenResponse tokenResponse = authService.issueTokensForPlainAuth(user);
    		
    		this.setSecureCookie(tokenResponse.getRefreshToken(), response);
    		return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse.toPublicResponse());
    		
    	} catch (IllegalArgumentException e) {
    		return ResponseEntity.status(HttpStatus.CONFLICT).build();
    	} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@CookieValue String refreshToken, HttpServletResponse response) {
    	try {    		
    		TokenResponse newTokenResponse = authService.reissueTokens(refreshToken);
    		
    		this.setSecureCookie(newTokenResponse.getRefreshToken(), response);
    		return ResponseEntity.ok(newTokenResponse.toPublicResponse());
    		
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    }
    
    private void setSecureCookie(String refreshToken, HttpServletResponse response) {
    	if (refreshToken == null) 
    		return;
    	
    	ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(refreshTokenValidTime / 1000) // maxAge is in seconds
				.sameSite("Strict")
				.build();
		
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
