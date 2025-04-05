package com.teamfour.smartexpense.controller;

import com.teamfour.smartexpense.dto.TransactionRequestDto;
import com.teamfour.smartexpense.dto.TransactionResponseDto;
import com.teamfour.smartexpense.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Create a new transaction.
     *
     * @param transactionRequest the request body containing transaction details
     * @return the created transaction with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto transactionRequest) {
        TransactionResponseDto response = transactionService.createTransaction(transactionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get a single transaction by its ID.
     *
     * @param id the transaction ID
     * @return the transaction details if found
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable Long id) {
        TransactionResponseDto response = transactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all transactions for a specific wallet.
     *
     * @param walletId the wallet ID
     * @return list of transactions linked to the given wallet
     */
    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByWallet(@PathVariable Long walletId) {
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByWallet(walletId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions for a wallet within a specific date-time range.
     *
     * @param walletId  the wallet ID
     * @param startDate the start date and time
     * @param endDate   the end date and time
     * @return list of transactions within the given time range
     */
    @GetMapping("/wallet/{walletId}/date-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByWalletAndDateRange(
            @PathVariable Long walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<TransactionResponseDto> transactions =
                transactionService.getTransactionsByWalletAndDateRange(walletId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Update a transaction by its ID.
     *
     * @param id                the transaction ID
     * @param transactionRequest the new transaction data
     * @return the updated transaction details
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDto transactionRequest) {
        TransactionResponseDto response = transactionService.updateTransaction(id, transactionRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a transaction by its ID.
     *
     * @param id the transaction ID
     * @return HTTP 204 No Content if deleted
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
