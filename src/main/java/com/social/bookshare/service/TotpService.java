package com.social.bookshare.service;

public interface TotpService {

    String generateNewSecret();
    String generateQrCodeDataUri(String secret, String userIdentifier);
    boolean match(String secret, String code);
}