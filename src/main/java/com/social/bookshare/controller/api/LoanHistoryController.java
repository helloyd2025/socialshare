package com.social.bookshare.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.social.bookshare.service.LoanHistoryService;
import com.social.bookshare.service.impl.LoanHistoryServiceImpl.LoanAction;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/books/{userBookId}")
public class LoanHistoryController {

	private LoanHistoryService loanHistoryService;
	
	public LoanHistoryController(LoanHistoryService loanHistoryService) {
		this.loanHistoryService = loanHistoryService;
	}
	
	@PostMapping("/loan/{actionType}")
    public ResponseEntity<Void> handleLoanAction(
            @PathVariable Long userBookId,
            @PathVariable String actionType,
            @RequestParam(value = "owner_id") Long ownerId,
            @RequestParam(value = "loaner_id") Long loanerId,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Integer loanDays) {
		
		LoanAction action = LoanAction.valueOf((actionType).toUpperCase());
		try {
			loanHistoryService.processAction(userBookId, ownerId, loanerId, action, loanDays, comment);
			return ResponseEntity.ok().build();
		} catch (AccessDeniedException | IllegalStateException | EntityNotFoundException e) {
			return ResponseEntity.badRequest().build();
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}
	}
}
