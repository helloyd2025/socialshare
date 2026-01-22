package com.social.bookshare.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.bookshare.domain.LoanHistory;
import com.social.bookshare.domain.LoanHistory.LoanStatus;
import com.social.bookshare.domain.User;
import com.social.bookshare.domain.UserBook;
import com.social.bookshare.dto.NotificationMessage;
import com.social.bookshare.dto.request.LoanRequest;
import com.social.bookshare.repository.LoanHistoryRepository;
import com.social.bookshare.service.LoanHistoryService;
import com.social.bookshare.service.NotificationService;
import com.social.bookshare.utils.EntityMapper;

import jakarta.persistence.EntityNotFoundException;

@Service
public class LoanHistoryServiceImpl implements LoanHistoryService {
	
	public enum LoanAction {
	    REQUEST_LOAN, APPROVE_LOAN, CANCEL_LOAN, REJECT_LOAN, CONFIRM_LOAN, VOID_LOAN,
	    REQUEST_RETURN, CANCEL_RETURN, CONFIRM_RETURN
	}

    private final LoanHistoryRepository loanHistoryRepository;
    private final NotificationService notificationService;
    private final EntityMapper entityMapper;
    private final RedissonClient redissonClient;
    
    private static final String PENDING_LOAN_PREFIX = "LOAN_HISTORY:LOAN:REQUEST:";
    private static final String RLOCK_PREFIX = "LOAN_HISTORY:LOCK:USERBOOK:";
	
	public LoanHistoryServiceImpl(LoanHistoryRepository loanHistoryRepository, NotificationService notificatinoService, 
			EntityMapper entityMapper, RedissonClient redissonClient) {
		this.loanHistoryRepository = loanHistoryRepository;
		this.notificationService = notificatinoService;
		this.entityMapper = entityMapper;
		this.redissonClient = redissonClient;
	}
	
	@Override
	@Transactional
	public void processAction(Long userBookId, Long ownerId, Long loanerId, LoanAction action, Integer expectedLoanDays, String comment) {
		UserBook userBook = entityMapper.getReference(UserBook.class, userBookId);
		User owner = (ownerId != null) ? entityMapper.getReference(User.class, ownerId) : null;
		User loaner = entityMapper.getReference(User.class, loanerId);
		
        switch (action) {
            case REQUEST_LOAN -> requestLoan(userBook, owner, loaner, expectedLoanDays, comment);
            case APPROVE_LOAN -> approveLoan(userBook, owner, loaner, comment);
            case CANCEL_LOAN, REJECT_LOAN -> cancelOrRejectLoan(userBook, owner, loaner, comment);
            case CONFIRM_LOAN -> confirmLoan(userBook, owner, loaner, comment);
            case VOID_LOAN -> voidLoan(userBook, owner, loaner, comment);
            case REQUEST_RETURN -> requestReturn(userBook, owner, loaner, comment);
            case CANCEL_RETURN -> cancelReturnRequest(userBook, owner, loaner, comment);
            case CONFIRM_RETURN -> confirmReturn(userBook, owner, loaner, comment);
            default -> throw new IllegalArgumentException("Unknown action");
        }
    }
	
	@Override
	@Transactional
	public void requestLoan(UserBook userBook, User owner, User loaner, int expectedLoanDays, String comment) {
		LoanRequest request = new LoanRequest(loaner.getId(), loaner.getName(), expectedLoanDays);
		
		RMapCache<Long, LoanRequest> requestMap = redissonClient.getMapCache(PENDING_LOAN_PREFIX + userBook.getId());
        requestMap.put(loaner.getId(), request, 3, TimeUnit.DAYS); // Wait up to 3 days
        
        this.notifyLoanRequest(loaner, owner, "LOAN:REQUEST", comment, userBook, expectedLoanDays);
	}
	
	@Override
	@Transactional
	public void approveLoan(UserBook userBook, User owner, User loaner, String comment) {
		if (!userBook.getOwnerId().equals(owner.getId())) {
			throw new AccessDeniedException("Only owner can approve loan request.");
		}
		RLock lock = redissonClient.getLock(RLOCK_PREFIX + userBook.getId()); // Attempt to acquire a lock
		
		RMapCache<Long, LoanRequest> requestMap = redissonClient.getMapCache(PENDING_LOAN_PREFIX + userBook.getId());
		Map<Long, LoanRequest> allRequests = requestMap.readAllMap();
		LoanRequest request = requestMap.get(loaner.getId());
		allRequests.remove(loaner.getId());
		
		if (request == null) {
			throw new IllegalStateException("Revoked or expired request");
		}
		try {
			if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) { // wait up to 5s, hold for 10s
				throw new IllegalStateException("Another process is currently in progress. Please try again later.");
			}
			// Approve loan
			userBook.approveLoan();
			
			LoanHistory history = LoanHistory.builder()
	                .userBook(userBook)
	                .loaner(loaner)
	                .loanDays(request.getExpectedLoanDays())
	                .build(); // Set loan status null yet
	        loanHistoryRepository.save(history);
			
			// Cancel all other requests
			List<LoanHistory> rejectedHistories = allRequests.values().stream()
		            .<LoanHistory>map(aor -> LoanHistory.builder()
		                    .userBook(userBook)
		                    .loaner(entityMapper.getReference(User.class, aor.getLoanerId()))
		                    .loanDays(aor.getExpectedLoanDays())
		                    .loanStatus(LoanStatus.PREOCCUPIED)
		                    .build())
		            .toList();
			
			if (!rejectedHistories.isEmpty()) {
				loanHistoryRepository.saveAll(rejectedHistories); // Bulk insert
			}
			requestMap.delete();
	        
			this.notifyLoanRequest(owner, loaner, "LOAN:APPROVE", comment, userBook, null);
			
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
        	if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
		}
	}
	
	@Override
	@Transactional
	public void cancelOrRejectLoan(UserBook userBook, User owner, User loaner, String comment) {
		RMap<Long, LoanRequest> requestMap = redissonClient.getMap(PENDING_LOAN_PREFIX + userBook.getId());
		LoanRequest canceledOrRejectedRequest = requestMap.remove(loaner.getId());
		
		if (canceledOrRejectedRequest == null) {
			throw new IllegalStateException("Canceled or expired request");
		} else if (requestMap.readAllMap().isEmpty()) {
			requestMap.delete();
		}
		
		LoanHistory history = LoanHistory.builder()
                .userBook(userBook)
                .loaner(loaner)
                .loanStatus(owner == null ? LoanStatus.CANCELED : LoanStatus.REJECTED)
                .build();
		loanHistoryRepository.save(history);
		
		if (owner != null) {
			this.notifyLoanRequest(owner, loaner, "LOAN:REJECT", comment, userBook, null);
		}
	}
	
	@Override
	@Transactional
	public void confirmLoan(UserBook userBook, User owner, User loaner, String comment) {
		if (!loanHistoryRepository.isLoanerApproved(userBook.getId(), loaner.getId())) {
			throw new AccessDeniedException("Only approved loaner can confirm loan.");
		}
		RLock lock = redissonClient.getLock(RLOCK_PREFIX + userBook.getId());
		
		try {
			if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
	            throw new IllegalStateException("Another process is currently in progress. Please try again later.");
	        }
			userBook.confirmLoan();
			
			LoanHistory history = loanHistoryRepository.findLoanHistoryApproved(userBook.getId(), loaner.getId())
					.orElseThrow(() -> new EntityNotFoundException("Loan history not found"));

			history.confirmLoan();
			this.notifyLoanRequest(loaner, owner, "LOAN:CONFIRM", comment, userBook, history.getLoanDays());
			
		} catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	    } finally {
	        if (lock.isHeldByCurrentThread()) {
	            lock.unlock();
	        }
	    }
	}
	
	@Override
	@Transactional
	public void voidLoan(UserBook userBook, User owner, User loaner, String comment) {
		if (!userBook.getOwnerId().equals(owner.getId())) {
			throw new AccessDeniedException("Only owner can void the contract.");
		}
		userBook.voidLoan();
		
		loanHistoryRepository.findLoanHistoryApproved(userBook.getId(), loaner.getId())
				.orElseThrow(() -> new EntityNotFoundException("Loan history not found"))
				.voidLoan();
		
		this.notifyLoanRequest(owner, loaner, "LOAN:VOID", comment, userBook, null);
	}

	@Override
	@Transactional
	public void requestReturn(UserBook userBook, User owner, User loaner, String comment) {
		if (!userBook.getOwnerId().equals(owner.getId())) {
			throw new AccessDeniedException("Only owner can request return.");
		}
		userBook.requestReturn();
		this.notifyLoanRequest(owner, loaner, "RETURN:REQUEST", comment, userBook, null);
	}

	@Override
	@Transactional
	public void cancelReturnRequest(UserBook userBook, User owner, User loaner, String comment) {
		if (!userBook.getOwnerId().equals(owner.getId())) {
			throw new AccessDeniedException("Only owner can cancel return request.");
		}
		userBook.cancelReturnRequest();
		this.notifyLoanRequest(owner, loaner, "RETURN:REQUEST:CANCEL", comment, userBook, null);
	}
	
	@Override
	@Transactional
	public void confirmReturn(UserBook userBook, User owner, User loaner, String comment) {
		if (!userBook.getOwnerId().equals(owner.getId())) {
			throw new AccessDeniedException("Only owner can approve return.");
		}
		userBook.confirmReturn();
		
		loanHistoryRepository.findLoanHistoryApproved(userBook.getId(), loaner.getId())
				.orElseThrow(() -> new EntityNotFoundException("Loan history not found"))
				.confirmReturn();
		
		this.notifyLoanRequest(owner, loaner, "RETURN:CONFIRM", comment, userBook, null);
	}
	
	private void notifyLoanRequest(User sender, User receiver, String type, String comment, UserBook userBook , Integer days) {
		Map<String, String> content = new HashMap<>();
        content.put("title", userBook.getBook().getTitle());
        if (sender != null) content.put("sender", sender.getName());
        if (days != null) content.put("days", String.valueOf(days));
		
		NotificationMessage message = NotificationMessage.builder()
				.receiverId(receiver.getId())
				.type(type)
				.message(comment)
				.content(content)
				.timestamp(LocalDateTime.now())
				.build();
		notificationService.sendNotification(message);
	}
}
