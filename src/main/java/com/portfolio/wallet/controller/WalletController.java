package com.portfolio.wallet.controller;

import com.portfolio.wallet.dto.DepositRequest;
import com.portfolio.wallet.dto.TransactionResponse;
import com.portfolio.wallet.dto.WalletResponse;
import com.portfolio.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable Long walletId) {
        return walletService.getWallet(walletId);
    }

    @PostMapping("/deposit")
    public WalletResponse deposit(@Valid @RequestBody DepositRequest request) {
        return walletService.deposit(request);
    }

    @GetMapping("/{walletId}/transactions")
    public List<TransactionResponse> getTransactions(@PathVariable Long walletId) {
        return walletService.getTransactionHistory(walletId);
    }
}
