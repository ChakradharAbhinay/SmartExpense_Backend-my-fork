package com.teamfour.smartexpense.service;


import com.teamfour.smartexpense.dto.TransactionResponseDto;
import com.teamfour.smartexpense.dto.WalletDTO;
import com.teamfour.smartexpense.model.Transaction;
import com.teamfour.smartexpense.model.User;
import com.teamfour.smartexpense.model.Wallet;
import com.teamfour.smartexpense.model.WalletType;
import com.teamfour.smartexpense.repository.TransactionRepository;
import com.teamfour.smartexpense.repository.UserRepository;
import com.teamfour.smartexpense.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<WalletDTO> getWalletsByUser(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        List<Wallet> wallets = walletRepository.findByUserId(user.getId());
        return wallets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WalletDTO getWalletById(Authentication authentication, Long walletId) {
        User user = getUserFromAuthentication(authentication);
        Optional<Wallet> wallet = walletRepository.findById(walletId);
        return wallet.filter(w -> w.getUser().getId().equals(user.getId()))
                .map(this::convertToDTO)
                .orElse(null);
    }

    public WalletDTO createWallet(Authentication authentication, WalletDTO walletDTO) {
        User user = getUserFromAuthentication(authentication);
        Wallet wallet = convertToEntity(walletDTO);
        wallet.setUser(user);
        Wallet savedWallet = walletRepository.save(wallet);
        return convertToDTO(savedWallet);
    }

    public WalletDTO updateWallet(Authentication authentication, Long walletId, WalletDTO walletDTO) {
        User user = getUserFromAuthentication(authentication);
        Optional<Wallet> existingWallet = walletRepository.findById(walletId);
        if (existingWallet.isPresent() && existingWallet.get().getUser().getId().equals(user.getId())) {
            Wallet wallet = existingWallet.get();
            wallet.setName(walletDTO.getName());
            wallet.setType(WalletType.valueOf(walletDTO.getType()));
            wallet.setBalance(walletDTO.getBalance());
            wallet.setCurrency(walletDTO.getCurrency());
            Wallet updatedWallet = walletRepository.save(wallet);
            return convertToDTO(updatedWallet);
        }
        return null;
    }

    public boolean deleteWallet(Authentication authentication, Long walletId) {
        User user = getUserFromAuthentication(authentication);
        Optional<Wallet> wallet = walletRepository.findById(walletId);
        if (wallet.isPresent() && wallet.get().getUser().getId().equals(user.getId())) {
            walletRepository.deleteById(walletId);
            return true;
        }
        return false;
    }
    
    private User getUserFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        WalletDTO dto = new WalletDTO();
        dto.setId(wallet.getId());
        dto.setName(wallet.getName());
        dto.setType(String.valueOf(wallet.getType()));
        dto.setBalance(wallet.getBalance());
        dto.setCurrency(wallet.getCurrency());
        List<Transaction> transactions = transactionRepository.findByWalletId(wallet.getId());
        dto.setTransactions(transactions.stream()
                .map(this::convertTransactionToDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private Wallet convertToEntity(WalletDTO dto) {
        Wallet wallet = new Wallet();
        wallet.setName(dto.getName());
        wallet.setType(WalletType.valueOf(dto.getType()));
        wallet.setBalance(dto.getBalance());
        wallet.setCurrency(dto.getCurrency());
        return wallet;
    }

    private TransactionResponseDto convertTransactionToDTO(Transaction transaction) {
        TransactionResponseDto dto = new TransactionResponseDto();
        dto.setId(transaction.getId());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setType(transaction.getType());
        return dto;
    }
}