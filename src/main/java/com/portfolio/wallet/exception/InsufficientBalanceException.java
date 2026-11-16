package com.portfolio.wallet.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(Long walletId, BigDecimal balance, BigDecimal amount) {
        super("Insufficient balance in wallet " + walletId
                + ": balance is " + balance + ", requested " + amount);
    }
}
