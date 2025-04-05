package com.teamfour.smartexpense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfour.smartexpense.dto.WalletDTO;
import com.teamfour.smartexpense.security.JwtUtil;
import com.teamfour.smartexpense.service.WalletService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    private final Authentication mockAuth = Mockito.mock(Authentication.class);

    private RequestPostProcessor withMockAuthentication() {
        return request -> {
            request.setUserPrincipal(mockAuth);
            return request;
        };
    }

    @Test
    void getAllWallets_returnsWalletList() throws Exception {
        WalletDTO wallet = new WalletDTO();
        wallet.setId(1L);
        wallet.setName("Primary");

        Mockito.when(walletService.getWalletsByUser(any(Authentication.class)))
                .thenReturn(List.of(wallet));

        mockMvc.perform(get("/api/wallets").with(withMockAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Primary"));
    }

    @Test
    void getWalletById_walletFound_returnsWallet() throws Exception {
        WalletDTO wallet = new WalletDTO();
        wallet.setId(1L);
        wallet.setName("Wallet A");

        Mockito.when(walletService.getWalletById(any(Authentication.class), eq(1L)))
                .thenReturn(wallet);

        mockMvc.perform(get("/api/wallets/1").with(withMockAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Wallet A"));
    }

    @Test
    void getWalletById_walletNotFound_returns404() throws Exception {
        Mockito.when(walletService.getWalletById(any(Authentication.class), eq(99L)))
                .thenReturn(null);

        mockMvc.perform(get("/api/wallets/99").with(withMockAuthentication()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createWallet_returnsCreatedWallet() throws Exception {
        WalletDTO walletToCreate = new WalletDTO();
        walletToCreate.setName("New Wallet");

        WalletDTO createdWallet = new WalletDTO();
        createdWallet.setId(5L);
        createdWallet.setName("New Wallet");

        Mockito.when(walletService.createWallet(any(Authentication.class), any(WalletDTO.class)))
                .thenReturn(createdWallet);

        mockMvc.perform(post("/api/wallets")
                        .with(withMockAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.name").value("New Wallet"));
    }

    @Test
    void updateWallet_walletFound_returnsUpdatedWallet() throws Exception {
        WalletDTO updatedWallet = new WalletDTO();
        updatedWallet.setId(3L);
        updatedWallet.setName("Updated");

        Mockito.when(walletService.updateWallet(any(Authentication.class), eq(3L), any(WalletDTO.class)))
                .thenReturn(updatedWallet);

        mockMvc.perform(put("/api/wallets/3")
                        .with(withMockAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedWallet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateWallet_walletNotFound_returns404() throws Exception {
        Mockito.when(walletService.updateWallet(any(Authentication.class), eq(42L), any(WalletDTO.class)))
                .thenReturn(null);

        WalletDTO dummy = new WalletDTO();
        dummy.setName("Doesn't matter");

        mockMvc.perform(put("/api/wallets/42")
                        .with(withMockAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummy)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWallet_walletFound_returns204() throws Exception {
        Mockito.when(walletService.deleteWallet(any(Authentication.class), eq(10L)))
                .thenReturn(true);

        mockMvc.perform(delete("/api/wallets/10").with(withMockAuthentication()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteWallet_walletNotFound_returns404() throws Exception {
        Mockito.when(walletService.deleteWallet(any(Authentication.class), eq(999L)))
                .thenReturn(false);

        mockMvc.perform(delete("/api/wallets/999").with(withMockAuthentication()))
                .andExpect(status().isNotFound());
    }
}
