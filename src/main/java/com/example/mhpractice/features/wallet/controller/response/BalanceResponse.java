package com.example.mhpractice.features.wallet.controller.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BalanceResponse {
    private BigDecimal balance;
}
