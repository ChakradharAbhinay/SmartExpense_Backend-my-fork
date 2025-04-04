package com.teamfour.smartexpense.repository;

import com.teamfour.smartexpense.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletId(Long walletId);
    List<Transaction> findByWalletIdAndDateBetween(Long walletId, LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByCategoryId(Long categoryId);
}
