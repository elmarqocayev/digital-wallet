package com.portfolio.wallet.dto;

import com.portfolio.wallet.entity.Currency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateUserRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Currency is required (AZN, USD, EUR)")
        Currency currency,

        @PositiveOrZero(message = "Initial balance cannot be negative")
        @Digits(integer = 15, fraction = 2, message = "Max 2 decimal places")
        BigDecimal initialBalance
) {}
