package com.social.bookshare.service.impl;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

import org.springframework.stereotype.Service;

import com.social.bookshare.service.TotpService;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

@Service
public class TotpServiceImpl implements TotpService {

    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;
    
    public TotpServiceImpl(SecretGenerator secretGenerator, QrDataFactory qrDataFactory,
    		QrGenerator qrGenerator, CodeVerifier codeVerifier) {
    	this.secretGenerator = secretGenerator;
    	this.qrDataFactory = qrDataFactory;
    	this.qrGenerator = qrGenerator;
    	this.codeVerifier = codeVerifier;
    }

    @Override
    public String generateNewSecret() {
        return secretGenerator.generate();
    }

    @Override
    public String generateQrCodeDataUri(String secret, String userIdentifier) {
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
    public boolean match(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}
