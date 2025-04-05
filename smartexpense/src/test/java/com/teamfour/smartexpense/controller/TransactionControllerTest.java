package com.teamfour.smartexpense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamfour.smartexpense.dto.TransactionRequestDto;
import com.teamfour.smartexpense.dto.TransactionResponseDto;
import com.teamfour.smartexpense.model.TransactionType;
import com.teamfour.smartexpense.security.JwtUtil;
import com.teamfour.smartexpense.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void createTransaction_ShouldReturnCreated() throws Exception {
        TransactionRequestDto request = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(100.0))
                .walletId(1L)
                .categoryId(1L)
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.now())
                .description("Lunch")
                .build();

        TransactionResponseDto response = TransactionResponseDto.builder()
                .id(1L)
                .amount(request.getAmount())
                .walletId(request.getWalletId())
                .categoryId(request.getCategoryId())
                .type(request.getType())
                .date(request.getDate())
                .description(request.getDescription())
                .build();

        Mockito.when(transactionService.createTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void getTransaction_ShouldReturnTransaction() throws Exception {
        TransactionResponseDto response = TransactionResponseDto.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(200.0))
                .walletId(2L)
                .categoryId(2L)
                .type(TransactionType.INCOME)
                .date(LocalDateTime.now())
                .description("Salary")
                .build();

        Mockito.when(transactionService.getTransaction(2L)).thenReturn(response);

        mockMvc.perform(get("/api/transactions/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }

    @Test
    @WithMockUser
    void getTransactionsByWallet_ShouldReturnList() throws Exception {
        TransactionResponseDto tx = TransactionResponseDto.builder()
                .id(3L)
                .walletId(3L)
                .amount(BigDecimal.TEN)
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.now())
                .description("Snack")
                .build();

        Mockito.when(transactionService.getTransactionsByWallet(3L)).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/transactions/wallet/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3L));
    }

    @Test
    @WithMockUser
    void getTransactionsByWalletAndDateRange_ShouldReturnList() throws Exception {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        TransactionResponseDto tx = TransactionResponseDto.builder()
                .id(4L)
                .walletId(4L)
                .date(LocalDateTime.now())
                .type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(50))
                .description("Gift")
                .build();

        Mockito.when(transactionService.getTransactionsByWalletAndDateRange(eq(4L), any(), any()))
                .thenReturn(List.of(tx));

        mockMvc.perform(get("/api/transactions/wallet/4/date-range")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4L));
    }

    @Test
    @WithMockUser
    void updateTransaction_ShouldReturnUpdated() throws Exception {
        TransactionRequestDto request = TransactionRequestDto.builder()
                .amount(BigDecimal.valueOf(20))
                .walletId(5L)
                .categoryId(5L)
                .type(TransactionType.EXPENSE)
                .date(LocalDateTime.now())
                .description("Coffee")
                .build();

        TransactionResponseDto response = TransactionResponseDto.builder()
                .id(5L)
                .amount(request.getAmount())
                .walletId(request.getWalletId())
                .categoryId(request.getCategoryId())
                .type(request.getType())
                .date(request.getDate())
                .description(request.getDescription())
                .build();

        Mockito.when(transactionService.updateTransaction(eq(5L), any())).thenReturn(response);

        mockMvc.perform(put("/api/transactions/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    @WithMockUser
    void deleteTransaction_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/transactions/6"))
                .andExpect(status().isNoContent());

        Mockito.verify(transactionService).deleteTransaction(6L);
    }
}
