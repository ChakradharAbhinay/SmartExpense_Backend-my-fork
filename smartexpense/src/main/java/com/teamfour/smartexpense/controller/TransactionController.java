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

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody TransactionRequestDto transactionRequest) {
        TransactionResponseDto response = transactionService.createTransaction(transactionRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponseDto> getTransaction(@PathVariable Long id) {
        TransactionResponseDto response = transactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallet/{walletId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByWallet(@PathVariable Long walletId) {
        List<TransactionResponseDto> transactions = transactionService.getTransactionsByWallet(walletId);
        return ResponseEntity.ok(transactions);
    }

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

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequestDto transactionRequest) {
        TransactionResponseDto response = transactionService.updateTransaction(id, transactionRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
