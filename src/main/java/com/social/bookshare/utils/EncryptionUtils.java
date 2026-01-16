package com.social.bookshare.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {
	
	private EncryptionUtils() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	private static final String ALGORITHM = "AES";
	
	public static String encrypt(String apiKey, String rawPassword) throws Exception {
        SecretKeySpec keySpec = EncryptionUtils.deriveKey(rawPassword);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(apiKey.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedKey, String rawPassword) throws Exception {
        SecretKeySpec keySpec = EncryptionUtils.deriveKey(rawPassword);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedKey);
        return new String(cipher.doFinal(decoded));
    }

    private static SecretKeySpec deriveKey(String password) throws Exception {
        // Generate a 256-bit key by hashing the password
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, ALGORITHM);
    }
}
