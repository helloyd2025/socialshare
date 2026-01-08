package com.social.bookshare.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.User.Role;
import com.social.bookshare.dto.request.LoginRequest;
import com.social.bookshare.dto.request.SignupRequest;
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
    public ResponseEntity<TokenResponse> login(HttpServletResponse response, @RequestBody LoginRequest request) {
    	try {
    		User user = userService.login(request, Role.USER);
    		TokenResponse tokenResponse = authService.issueTokens(user);
    		
    		this.setSecureCookie(tokenResponse.getRefreshToken(), response);
            return ResponseEntity.ok(new TokenResponse(tokenResponse.getAccessToken()));
            
    	} catch (BadCredentialsException | UsernameNotFoundException e) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(HttpServletResponse response, @RequestBody SignupRequest request) {
    	try {
    		User user = userService.signup(request, Role.USER);
    		TokenResponse tokenResponse = authService.issueTokens(user);
    		
    		this.setSecureCookie(tokenResponse.getRefreshToken(), response);
    		
    		return ResponseEntity.status(HttpStatus.CREATED)
    				.body(new TokenResponse(tokenResponse.getAccessToken()));
    		
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
    		return ResponseEntity.ok(new TokenResponse(newTokenResponse.getAccessToken()));
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    }
    
    private void setSecureCookie(String refreshToken, HttpServletResponse response) {
    	ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
				.httpOnly(true)
				.secure(true)
				.path("/")
				.maxAge(refreshTokenValidTime * 1000)
				.sameSite("Strict")
				.build();
		
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}
