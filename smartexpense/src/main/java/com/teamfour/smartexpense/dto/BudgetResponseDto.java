package com.teamfour.smartexpense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponseDto {
    private Long id;
    private BigDecimal amount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long categoryId;
    private String categoryName;
    private String categoryType;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private double percentageUsed;
}
