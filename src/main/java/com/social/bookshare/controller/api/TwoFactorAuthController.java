package com.social.bookshare.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.config.security.PrincipalDetails;
import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.TotpVerificationRequest;
import com.social.bookshare.dto.response.TotpSetupResponse;
import com.social.bookshare.service.TotpService;
import com.social.bookshare.utils.EntityMapper;

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
        User user = EntityMapper.getReference(User.class, principalDetails.getId());

        if (user.isTfaEnabled())  // If 2FA already enabled, further setup blocked... 
             return ResponseEntity.badRequest().build();

        // Create secret and QR code
        final String secret = totpService.generateNewSecret();
        final String qrCodeUri = totpService.generateQrCodeDataUri(secret, user.getEmail());

        user.updateTfaSecret(secret); // 2FA not activated yet
        return ResponseEntity.ok(new TotpSetupResponse(secret, qrCodeUri));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> tfaVerify(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody TotpVerificationRequest verificationRequest) {
    	
    	User user = EntityMapper.getReference(User.class, principalDetails.getId());

        // Check TOTP code
        if (!totpService.matches(user.getTfaSecret(), verificationRequest.getCode())) 
            return ResponseEntity.badRequest().build();

        user.updateIsTfaEnabled(true); // 2FA activated finally
        return ResponseEntity.ok().build();
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> tfaDisable(@AuthenticationPrincipal PrincipalDetails principalDetails) {
    	User user = EntityMapper.getReference(User.class, principalDetails.getId());

    	user.updateTfaSecret(null);
    	user.updateIsTfaEnabled(false);

        return ResponseEntity.ok().build();
    }
}