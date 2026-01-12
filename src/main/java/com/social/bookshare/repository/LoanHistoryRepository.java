package com.social.bookshare.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.social.bookshare.domain.LoanHistory;
import com.social.bookshare.domain.User;

public interface LoanHistoryRepository extends JpaRepository<LoanHistory, Long> {

	@Query(value = "SELECT lh.loaner FROM LoanHistory lh"
			+ " WHERE lh.userBook.id = :userBookId"
			+ " AND lh.returnDate IS NULL"
			+ " AND lh.userBook.status IN (com.social.bookshare.domain.UserBook.Status.PENDING_LOAN,"
			+ "							   com.social.bookshare.domain.UserBook.Status.LOANED,"
			+ "                            com.social.bookshare.domain.UserBook.Status.PENDING_RETURN)")
    public Optional<User> findCurrentOccupant(@Param("userBookId") Long userBookId);
	
	@Query(value = "SELECT COUNT(lh) > 0 FROM LoanHistory lh"
			+ " WHERE lh.userBook.id = :userBookId"
			+ " AND lh.loanStatus IS NULL"
			+ " AND lh.loaner.id = :loanerId"
			+ " AND lh.loanDate IS NULL")
	public boolean isLoanerApproved(@Param("userBookId") Long userBookId, @Param("loanerId") Long loanerId);
	
	@Query(value = "SELECT lh FROM LoanHistory lh"
			+ " WHERE lh.userBook.id = :userBookId"
			+ " AND lh.loanStatus IS NULL"
			+ " AND lh.loaner.id = :loanerId"
			+ " AND lh.loanDate IS NULL")
	public Optional<LoanHistory> findLoanHistoryApproved(@Param("userBookId") Long userBookId, @Param("loanerId") Long loanerId);
}