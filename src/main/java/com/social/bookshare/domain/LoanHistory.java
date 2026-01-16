package com.social.bookshare.domain;

import java.time.LocalDateTime;

import com.social.bookshare.config.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "loan_histories")
public class LoanHistory extends BaseEntity {
	
	protected LoanHistory() {}
	
	private LoanHistory(Builder builder) {
		this.userBook = builder.userBook;
		this.loaner = builder.loaner;
		this.loanDays = builder.loanDays;
		this.loanStatus = builder.loanStatus;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_book_id", referencedColumnName = "id", nullable = false)
    private UserBook userBook;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loaner_id", referencedColumnName = "id", nullable = false)
    private User loaner;
    
    @Column(name = "loan_date")
    private LocalDateTime loanDate;
    
    @Min(value = 1)
    @Column(name = "loan_days", nullable = false)
    private Integer loanDays = 14;
    
    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "loan_status", length = 20)
    private LoanStatus loanStatus;
    
    public enum LoanStatus {
    	LOANED, RETURNED, CANCELED, REJECTED, PREOCCUPIED
    }
    
    public void confirmLoan() {
    	this.loanDate = LocalDateTime.now();
    	this.loanStatus = LoanStatus.LOANED;
    }
    
    public void confirmReturn() {
        this.returnDate = LocalDateTime.now();
        this.loanStatus = LoanStatus.RETURNED;
    }
    
    public void voidLoan() {
    	if (this.loanStatus != null) throw new IllegalStateException("You cannot set history already set");
    	this.loanStatus = LoanStatus.REJECTED;
    }

    public boolean isOverdue() {
        return returnDate == null && LocalDateTime.now().isAfter(loanDate.plusDays(loanDays));
    }
    
    // Getters
	public Long getId() { return id; }
	public UserBook getUserBook() { return userBook; }
	public User getLoaner() { return loaner; }
	public LocalDateTime getLoanDate() { return loanDate; }
	public Integer getLoanDays() { return loanDays; }
	public LocalDateTime getReturnDate() { return returnDate; }
	public LoanStatus getLoanStatus() { return loanStatus; }
	
	// Builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
    	private UserBook userBook;
    	private User loaner;
    	private Integer loanDays;
        private LoanStatus loanStatus;
        
        public Builder userBook(UserBook userBook) {
        	this.userBook = userBook;
        	return this;
        }
        
        public Builder loaner(User loaner) {
        	this.loaner = loaner;
        	return this;
        }
        
        public Builder loanDays(Integer loanDays) {
        	this.loanDays = loanDays;
        	return this;
        }
        
        public Builder loanStatus(LoanStatus loanStatus) {
        	this.loanStatus = loanStatus;
        	return this;
        }
        
        public Builder loanStatus(String loanStatus) {
        	this.loanStatus = LoanStatus.valueOf(loanStatus.strip().toUpperCase());
        	return this;
        }
        
        public LoanHistory build() {
            return new LoanHistory(this);
        }
    }
}
