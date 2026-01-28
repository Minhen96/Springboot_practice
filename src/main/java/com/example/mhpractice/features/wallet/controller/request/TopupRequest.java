package com.example.mhpractice.features.wallet.controller.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopupRequest {
    @NotNull
    @DecimalMin(value = "0.01", inclusive = false)
    private BigDecimal amount;
}
