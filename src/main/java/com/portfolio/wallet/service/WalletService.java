package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.DepositRequest;
import com.portfolio.wallet.dto.TransactionResponse;
import com.portfolio.wallet.dto.WalletResponse;
import com.portfolio.wallet.entity.Transaction;
import com.portfolio.wallet.entity.TransactionStatus;
import com.portfolio.wallet.entity.Wallet;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.repository.TransactionRepository;
import com.portfolio.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;


    @Transactional
    public WalletResponse deposit(DepositRequest request) {
        Wallet wallet = walletRepository.findByIdForUpdate(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException(request.walletId()));

        wallet.setBalance(wallet.getBalance().add(request.amount()));

        transactionRepository.save(Transaction.builder()
                .senderWalletId(null)
                .receiverWalletId(wallet.getId())
                .amount(request.amount())
                .status(TransactionStatus.SUCCESS)
                .build());

        return WalletResponse.from(wallet);
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return WalletResponse.from(wallet);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(Long walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException(walletId);
        }
        return transactionRepository
                .findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(walletId, walletId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
