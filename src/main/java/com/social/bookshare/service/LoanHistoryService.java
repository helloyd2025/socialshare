package com.social.bookshare.service;

import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserBook;
import com.social.bookshare.service.impl.LoanHistoryServiceImpl.LoanAction;

public interface LoanHistoryService {
	
	public void processAction(Long userBookId, Long ownerId, Long loanerId, LoanAction action, Integer expectedLoanDays, String comment);
	
	public void requestLoan(UserBook userBook, User owner, User loaner, int expectedLoanDays, String comment);
	public void approveLoan(UserBook userBook, User owner, User loaner, String comment);
	public void cancelOrRejectLoan(UserBook userBook, User owner, User loaner, String comment);
	public void confirmLoan(UserBook userBook, User owner, User loaner, String comment);
	public void voidLoan(UserBook userBook, User owner, User loaner, String comment);
	public void requestReturn(UserBook userBook, User owner, User loaner, String comment);
	public void cancelReturnRequest(UserBook userBook, User owner, User loaner, String comment);
	public void confirmReturn(UserBook userBook, User owner, User loaner, String comment);
}
