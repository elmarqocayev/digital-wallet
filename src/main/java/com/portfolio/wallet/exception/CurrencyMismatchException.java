package com.portfolio.wallet.exception;

import com.portfolio.wallet.entity.Currency;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(Currency senderCurrency, Currency receiverCurrency) {
        super("Currency mismatch: sender wallet is " + senderCurrency
                + ", receiver wallet is " + receiverCurrency);
    }
}
