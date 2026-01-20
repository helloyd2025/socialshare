package com.social.bookshare.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.config.security.PrincipalDetails;
import com.social.bookshare.dto.request.TotpVerificationRequest;
import com.social.bookshare.dto.response.TotpSetupResponse;
import com.social.bookshare.service.TotpService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/auth/2fa")
public class TwoFactorAuthController {

    private final TotpService totpService;
    
    public TwoFactorAuthController(TotpService totpService) {
    	this.totpService = totpService;
    }

    @PostMapping("/setup")
    public ResponseEntity<TotpSetupResponse> tfaSetup(@AuthenticationPrincipal PrincipalDetails principalDetails) {
    	// Start setting up 2FA, generate a QR code.
        try {
        	TotpSetupResponse response = totpService.setupTfa(principalDetails.getId());
        	return ResponseEntity.ok(response);
        } catch (EntityNotFoundException | IllegalStateException e) {
        	return ResponseEntity.badRequest().build(); 
        } catch (Exception e) {
        	return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> tfaVerify(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody TotpVerificationRequest request) {
    	try {
    		totpService.verifyTfa(principalDetails.getId(), request);
    		return ResponseEntity.ok().build();
    	} catch (EntityNotFoundException | IllegalStateException | BadCredentialsException e) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	} catch (Exception e) {
    		return ResponseEntity.internalServerError().build();
    	}
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> tfaDisable(@AuthenticationPrincipal PrincipalDetails principalDetails) {
    	try {
    		totpService.disableTfa(principalDetails.getId());
    		return ResponseEntity.ok().build();
    	} catch (IllegalStateException e) {
    		return ResponseEntity.badRequest().build();
    	} catch (Exception e) {
    		return ResponseEntity.internalServerError().build();
    	}
    }
}