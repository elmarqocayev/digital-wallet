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

    /**
     * Adds money to a wallet and records it in the history.
     * Both steps are in one transaction: either both happen or neither does.
     */
    @Transactional
    public WalletResponse deposit(DepositRequest request) {
        // 1. Load the wallet WITH a row lock so concurrent deposits cannot overwrite each other.
        Wallet wallet = walletRepository.findByIdForUpdate(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException(request.walletId()));

        // 2. Increase the balance. BigDecimal is immutable, so add() returns a new value.
        //    Hibernate notices the change ("dirty checking") and runs the UPDATE at commit.
        wallet.setBalance(wallet.getBalance().add(request.amount()));

        // 3. Save a history record. Sender is null because the money comes from outside.
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
        // Return 404 for an unknown wallet instead of a misleading empty list.
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
