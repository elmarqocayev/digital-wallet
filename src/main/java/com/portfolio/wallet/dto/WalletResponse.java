package com.portfolio.wallet.dto;

import com.portfolio.wallet.entity.Currency;
import com.portfolio.wallet.entity.Wallet;

import java.math.BigDecimal;

public record WalletResponse(Long id, Long userId, BigDecimal balance, Currency currency) {

    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getBalance(),
                wallet.getCurrency());
    }
}
