package com.teamfour.smartexpense.service;

import com.teamfour.smartexpense.dto.TransactionRequestDto;
import com.teamfour.smartexpense.dto.TransactionResponseDto;
import com.teamfour.smartexpense.exception.ResourceNotFoundException;
import com.teamfour.smartexpense.model.Category;
import com.teamfour.smartexpense.model.Transaction;
import com.teamfour.smartexpense.model.TransactionType;
import com.teamfour.smartexpense.model.Wallet;
import com.teamfour.smartexpense.repository.BudgetRespository;
import com.teamfour.smartexpense.repository.CategoryRepository;
import com.teamfour.smartexpense.repository.TransactionRepository;
import com.teamfour.smartexpense.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetService budgetService;


    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository, CategoryRepository categoryRepository, BudgetService budgetService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.categoryRepository = categoryRepository;
        this.budgetService = budgetService;
    }

    @Transactional
    public TransactionResponseDto createTransaction(TransactionRequestDto requestDto) {
        // Get wallet
        Wallet wallet = walletRepository.findById(requestDto.getWalletId())
                .orElseThrow(()-> new ResourceNotFoundException("Wallet not found with id: " + requestDto.getWalletId()));

        // Get Category if provided
        Category category = null;
        if (requestDto.getCategoryId() != null) {
            category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
        }

        // Create transaction
        Transaction transaction = Transaction.builder()
                .amount(requestDto.getAmount())
                .description(requestDto.getDescription())
                .date(requestDto.getDate())
                .type(requestDto.getType())
                .wallet(wallet)
                .category(category)
                .build();

        // Update wallet balance
        updateWalletBalance(wallet, transaction);

        //Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Check budget if it's an expense and category is set
        if (category != null && requestDto.getType() == TransactionType.EXPENSE) {
            budgetService.checkBudgetLimits(wallet.getUser().getId(), category.getId(), requestDto.getAmount());
        }

        return mapToDto(savedTransaction);
    }

    @Transactional
    public TransactionResponseDto updateTransaction(Long id, TransactionRequestDto requestDto) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Revert the previous wallet balance update
        updateWalletBalance(transaction.getWallet(), transaction, true);

        // Get wallet
        Wallet wallet = walletRepository.findById(requestDto.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + requestDto.getWalletId()));

        // Get category if provided
        Category category = null;
        if (requestDto.getCategoryId() != null) {
            category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
        }

        // Update transaction entity
        transaction.setAmount(requestDto.getAmount());
        transaction.setDescription(requestDto.getDescription());
        transaction.setDate(requestDto.getDate());
        transaction.setType(requestDto.getType());
        transaction.setWallet(wallet);
        transaction.setCategory(category);

        // Update wallet balance with new transaction
        updateWalletBalance(wallet, transaction);

        // Save updated transaction
        Transaction updatedTransaction = transactionRepository.save(transaction);

        // Check budget if it's an expense and category is set
        if (category != null && requestDto.getType() == TransactionType.EXPENSE) {
            budgetService.checkBudgetLimits(wallet.getUser().getId(), category.getId(), requestDto.getAmount());
        }

        return mapToDto(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Revert the wallet balance
        updateWalletBalance(transaction.getWallet(), transaction, true);

        // Delete transaction
        transactionRepository.delete(transaction);
    }

    public TransactionResponseDto getTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        return mapToDto(transaction);
    }

    public List<TransactionResponseDto> getTransactionsByWallet(Long walletId) {
        List<Transaction> transactions = transactionRepository.findByWalletId(walletId);
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDto> getTransactionsByWalletAndDateRange(Long walletId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Transaction> transactions = transactionRepository.findByWalletIdAndDateBetween(walletId, startDate, endDate);
        return transactions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void updateWalletBalance(Wallet wallet, Transaction transaction) {
        updateWalletBalance(wallet, transaction, false);
    }

    private void updateWalletBalance(Wallet wallet, Transaction transaction, boolean isReverse) {
        BigDecimal amount = transaction.getAmount();

        // Apply reverse effect if needed (for updates or deletes)
        if (isReverse) {
            amount = amount.negate();
        }

        // Update wallet balance based on transaction type
        switch (transaction.getType()) {
            case INCOME:
                wallet.setBalance(wallet.getBalance().add(isReverse ? amount.negate() : amount));
                break;
            case EXPENSE:
                wallet.setBalance(wallet.getBalance().subtract(isReverse ? amount.negate() : amount));
                break;
            case TRANSFER:
                // For transfers, we would need to handle both source and destination wallets
                // This is simplified and would need additional logic for transfers
                break;
        }

        walletRepository.save(wallet);
    }

    private TransactionResponseDto mapToDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .type(transaction.getType())
                .walletId(transaction.getWallet().getId())
                .walletName(transaction.getWallet().getName())
                .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                .createdAt(transaction.getCreatedAt())
                .modifiedAt(transaction.getModifiedAt())
                .build();
    }
}