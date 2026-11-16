package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.TransactionResponse;
import com.portfolio.wallet.dto.TransferRequest;
import com.portfolio.wallet.entity.Transaction;
import com.portfolio.wallet.entity.TransactionStatus;
import com.portfolio.wallet.entity.Wallet;
import com.portfolio.wallet.exception.CurrencyMismatchException;
import com.portfolio.wallet.exception.InsufficientBalanceException;
import com.portfolio.wallet.exception.InvalidTransferException;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.repository.TransactionRepository;
import com.portfolio.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionRecorder transactionRecorder;


    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        Long senderId = request.senderWalletId();
        Long receiverId = request.receiverWalletId();
        BigDecimal amount = request.amount();

        if (senderId.equals(receiverId)) {
            throw new InvalidTransferException("Sender and receiver wallets must be different");
        }


        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);
        Wallet first = lockWallet(firstId);
        Wallet second = lockWallet(secondId);

        Wallet sender = senderId.equals(first.getId()) ? first : second;
        Wallet receiver = (sender == first) ? second : first;

        if (sender.getCurrency() != receiver.getCurrency()) {
            transactionRecorder.recordFailure(senderId, receiverId, amount);
            throw new CurrencyMismatchException(sender.getCurrency(), receiver.getCurrency());
        }


        if (sender.getBalance().compareTo(amount) < 0) {
            transactionRecorder.recordFailure(senderId, receiverId, amount);
            throw new InsufficientBalanceException(senderId, sender.getBalance(), amount);
        }


        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        Transaction saved = transactionRepository.save(Transaction.builder()
                .senderWalletId(senderId)
                .receiverWalletId(receiverId)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .build());

        return TransactionResponse.from(saved);
    }

    private Wallet lockWallet(Long walletId) {
        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}
