package com.social.bookshare.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtils {
	
	private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;

    private final String systemKey;
    private final SecureRandom secureRandom;

    public EncryptionUtils(@Value("${secret.encryption.key}") String systemKey, SecureRandom secureRandom) {
        this.systemKey = systemKey;
        this.secureRandom = secureRandom;
    }
    
    public byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
    
//	public String encrypt(String userKey, byte[] salt, String plainText) throws Exception {
//        return encipher(deriveKey(userKey, salt), plainText);
//    }
//
//    public String decrypt(String userKey, byte[] salt, String encryptedDataWithIv) throws Exception {
//    	return decipher(deriveKey(userKey, salt), encryptedDataWithIv);
//    }

    private SecretKeySpec deriveKey(String userKey, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(userKey.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    // --- System-wide Key Encryption/Decryption ---

    public String encryptWithSystemKey(String plainText) throws Exception {
        return encipher(deriveSystemKey(), plainText);
    }

    public String decryptWithSystemKey(String encryptedDataWithIv) throws Exception {
    	return decipher(deriveSystemKey(), encryptedDataWithIv);
    }

    private SecretKeySpec deriveSystemKey() {
        byte[] keyBytes = Base64.getDecoder().decode(systemKey);
        // Ensure the key length is 256 bits for AES-256
        if (keyBytes.length != KEY_LENGTH / 8) {
            throw new IllegalArgumentException("Invalid system encryption key length. Must be 32 bytes after Base64 decoding.");
        } else {
        	return new SecretKeySpec(keyBytes, "AES");
        }
    }
    
    // --- Hybrid Encryption/Decryption ---
    
    public String encryptHybrid(String userKey, byte[] salt, String plainText) throws Exception {
        return encipher(deriveCombinedKey(userKey, salt), plainText);
    }

    public String decryptHybrid(String userKey, byte[] salt, String encryptedData) throws Exception {
        return decipher(deriveCombinedKey(userKey, salt), encryptedData);
    }

    private SecretKeySpec deriveCombinedKey(String userKey, byte[] salt) throws Exception {
        byte[] uKey = deriveKey(userKey, salt).getEncoded();
        byte[] sKey = deriveSystemKey().getEncoded();

        byte[] combined = new byte[32];
        for (int i = 0; i < 32; i++) {
            combined[i] = (byte) (uKey[i] ^ sKey[i]);
        }
        return new SecretKeySpec(combined, "AES");
    }
    
    // --- base ---
    
	private String encipher(SecretKeySpec keySpec, String plainText) throws Exception {
		byte[] iv = new byte[IV_LENGTH_BYTE];
        secureRandom.nextBytes(iv); // Random IV

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Combine IV and CipherText
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }
	
	private String decipher(SecretKeySpec keySpec, String encryptedDataWithIv) throws Exception {
    	byte[] decoded = Base64.getDecoder().decode(encryptedDataWithIv);

        // Separate IV and CipherText
        ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[IV_LENGTH_BYTE];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }
}
