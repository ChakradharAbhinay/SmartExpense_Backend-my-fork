package com.teamfour.smartexpense.dto;


import com.teamfour.smartexpense.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String description;

    @NotNull(message = "Transaction date is required")
    private LocalDateTime date;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Wallet id is required")
    private Long walletId;

    private Long categoryId;
}
