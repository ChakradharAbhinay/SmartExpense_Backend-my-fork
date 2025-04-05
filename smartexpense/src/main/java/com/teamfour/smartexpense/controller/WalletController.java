package com.teamfour.smartexpense.controller;

import com.teamfour.smartexpense.dto.WalletDTO;
import com.teamfour.smartexpense.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * Get all wallets associated with the authenticated user.
     *
     * @param authentication the authenticated user
     * @return list of wallets
     */
    @GetMapping
    public ResponseEntity<List<WalletDTO>> getAllWallets(Authentication authentication) {
        List<WalletDTO> wallets = walletService.getWalletsByUser(authentication);
        return new ResponseEntity<>(wallets, HttpStatus.OK);
    }

    /**
     * Get a wallet by ID if it belongs to the authenticated user.
     *
     * @param authentication the authenticated user
     * @param id the wallet ID
     * @return wallet data if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<WalletDTO> getWalletById(Authentication authentication, @PathVariable Long id) {
        WalletDTO wallet = walletService.getWalletById(authentication, id);
        if (wallet != null) {
            return new ResponseEntity<>(wallet, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Create a new wallet for the authenticated user.
     *
     * @param authentication the authenticated user
     * @param walletDTO the wallet data to be created
     * @return the created wallet with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<WalletDTO> createWallet(Authentication authentication, @RequestBody WalletDTO walletDTO) {
        WalletDTO createdWallet = walletService.createWallet(authentication, walletDTO);
        return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
    }

    /**
     * Update an existing wallet owned by the authenticated user.
     *
     * @param authentication the authenticated user
     * @param id the wallet ID
     * @param walletDTO updated wallet data
     * @return updated wallet or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<WalletDTO> updateWallet(Authentication authentication, @PathVariable Long id, @RequestBody WalletDTO walletDTO) {
        WalletDTO updatedWallet = walletService.updateWallet(authentication, id, walletDTO);
        if (updatedWallet != null) {
            return new ResponseEntity<>(updatedWallet, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Delete a wallet owned by the authenticated user.
     *
     * @param authentication the authenticated user
     * @param id the wallet ID
     * @return HTTP 204 if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(Authentication authentication, @PathVariable Long id) {
        boolean deleted = walletService.deleteWallet(authentication, id);
        if (deleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
