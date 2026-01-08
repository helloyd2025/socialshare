package com.social.bookshare.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class BookUtils {
	
	public BookUtils() {
		// If dependencies required, fill here..
	}

	public static String generateBookHash(String title, Short volume, String author, String publisher) {
		if (Stream.of(title, author, publisher).anyMatch(s -> s == null || s.isBlank()) || volume == null) 
			throw new IllegalArgumentException("Must be filled in: title, author, publisher");
		
		try {
			String input = (title + volume + author + publisher).replaceAll("\\s+", "").toLowerCase();
			
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
	        
	        String base64Hash = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
	        
	        return "U" + base64Hash.substring(0, 12).toUpperCase();
	        
		} catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("Hash algorithm not found", e);
	    }
	}
}
