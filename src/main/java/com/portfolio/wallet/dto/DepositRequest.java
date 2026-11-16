package com.portfolio.wallet.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull(message = "walletId is required")
        Long walletId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than 0")
        @Digits(integer = 15, fraction = 2, message = "Max 2 decimal places")
        BigDecimal amount
) {}
