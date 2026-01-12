package com.social.bookshare.dto.request;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoanRequest {
	
	public LoanRequest(Long loanerId, String loanerName, int expectedLoanDays) {
		this.loanerId = loanerId;
		this.loanerName = loanerName;
		this.expectedLoanDays = expectedLoanDays;
		this.requestedAt = LocalDateTime.now();
	}

	@JsonProperty("loaner_id")
	private Long loanerId;
	
	@JsonProperty("loaner_name")
	private String loanerName;
	
	@JsonProperty("expected_loan_days")
	private int expectedLoanDays;
	
	@JsonProperty("requested_at")
	private LocalDateTime requestedAt;
	
	public Long getLoanerId() { return loanerId; }
	public String getLoanerEmail() { return loanerName; }
	public int getExpectedLoanDays() { return expectedLoanDays; }
	public LocalDateTime getRequestedAt() { return requestedAt; }
}
