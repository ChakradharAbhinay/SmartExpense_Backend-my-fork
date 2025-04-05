package com.teamfour.smartexpense.dto;


import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WalletDTO {
    private Long id;
    private String name;
    private String type;
    private BigDecimal balance;
    private String currency;
    private List<TransactionResponseDto> transactions;
}