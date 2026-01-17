package com.social.bookshare.dto;

public class DecryptionResult {
	
	public DecryptionResult(String plainText, boolean isUpgradeRequired) {
		this.plainText = plainText;
		this.isUpgradeRequired = isUpgradeRequired;
	}

	String plainText;
	boolean isUpgradeRequired;
	
	public String getPlainText() { return plainText; }
	public boolean isUpgradeRequired() { return isUpgradeRequired; }
}
