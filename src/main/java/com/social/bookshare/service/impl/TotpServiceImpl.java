package com.social.bookshare.service.impl;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.User;
import com.social.bookshare.dto.request.TotpVerificationRequest;
import com.social.bookshare.dto.response.TotpSetupResponse;
import com.social.bookshare.repository.UserRepository;
import com.social.bookshare.service.TotpService;
import com.social.bookshare.utils.EncryptionUtils;
import com.social.bookshare.utils.EntityMapper;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import jakarta.persistence.EntityNotFoundException;

@Service
public class TotpServiceImpl implements TotpService {

    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;
    private final UserRepository userRepository;
    
    public TotpServiceImpl(SecretGenerator secretGenerator, QrDataFactory qrDataFactory, 
    		QrGenerator qrGenerator, CodeVerifier codeVerifier, UserRepository userRepository) {
    	this.secretGenerator = secretGenerator;
    	this.qrDataFactory = qrDataFactory;
    	this.qrGenerator = qrGenerator;
    	this.codeVerifier = codeVerifier;
    	this.userRepository = userRepository;
    }

    private String generateQrCodeDataUri(String secret, String userIdentifier) {
        try {
        	QrData data = qrDataFactory.newBuilder()
            		.label(userIdentifier) // Identifier for user
            		.secret(secret)
            		.issuer("Bookshare") // Service name
            		.build();
        	
            // Generates QR code as PNG image bytes.
            byte[] qrCodeImage = qrGenerator.generate(data);
            // Converts the generated image bytes into a Data URI string and returns it.
            return getDataUriForImage(qrCodeImage, qrGenerator.getImageMimeType());
            
        } catch (QrGenerationException e) {
            throw new RuntimeException("QR Code generation failed", e);
        }
    }
    
    @Override
    @Transactional
    public TotpSetupResponse setupTfa(Long userId) {
    	User user = userRepository.findById(userId)
    			.orElseThrow(() -> new EntityNotFoundException("User not found"));
    	
    	if (user.isTfaEnabled()) {
    		throw new IllegalStateException("2FA already enabled. Further setup blocked.");
    	}
    	
    	final String secret = secretGenerator.generate();
    	final String qrCodeUri = generateQrCodeDataUri(secret, user.getEmail());
    	
    	try {
        	String encryptedSecret = EncryptionUtils.encryptWithSystemKey(secret);
        	user.updateTfaSecret(encryptedSecret); // Set 2FA secret only (not activated yet)
    	} catch (Exception e) {
            throw new RuntimeException("2FA secret encryption failed", e);
    	}
    	
    	return new TotpSetupResponse(secret, qrCodeUri);
    }
    
    @Override
    @Transactional(readOnly = true)
    public User authenticateTfa(Long userId, String code) {
    	User user = userRepository.findById(userId)
    			.orElseThrow(() -> new UsernameNotFoundException("User not found in token."));
    	
    	if (!user.isTfaEnabled()) throw new IllegalStateException("2FA is not enabled for this user.");

		String encryptedSecret = user.getTfaSecret();
		String decryptedSecret;
		
		try {
			decryptedSecret = EncryptionUtils.decryptWithSystemKey(encryptedSecret);
		} catch (Exception e) {
			throw new RuntimeException("2FA secret decryption failed", e);
		}
		
		if (!codeVerifier.isValidCode(decryptedSecret, code)) {
			throw new BadCredentialsException("Invalid 2FA code");
		} else {
			return user;
		}
    }
    
    @Override
    @Transactional
    public void verifyTfa(Long userId, TotpVerificationRequest request) {
    	User user = userRepository.findById(userId)
    			.orElseThrow(() -> new EntityNotFoundException("User not found"));
    	
    	if (user.getTfaSecret() == null) throw new IllegalStateException("2FA setup not initiated");
    	
    	String encryptedSecret = user.getTfaSecret();
        String decryptedSecret;
        
		try {
			decryptedSecret = EncryptionUtils.decryptWithSystemKey(encryptedSecret);
		} catch (Exception e) {
			throw new RuntimeException("2FA secret decryption failed", e);
		}
        
        if (!codeVerifier.isValidCode(decryptedSecret, request.getCode())) {
        	throw new BadCredentialsException("Invalid access");
        } else {
        	user.updateIsTfaEnabled(true); // Set 2FA activated here
        }
    }

	@Override
	@Transactional
	public void disableTfa(Long userId) {
		User user = EntityMapper.getReference(User.class, userId);
		
		if (!user.isTfaEnabled()) {
			throw new IllegalStateException("2FA not enabled");
		}

    	user.updateTfaSecret(null);
    	user.updateIsTfaEnabled(false);
	}
}

