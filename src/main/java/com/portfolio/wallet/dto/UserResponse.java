package com.portfolio.wallet.dto;

import com.portfolio.wallet.entity.User;

public record UserResponse(Long id, String fullName, String email, WalletResponse wallet) {

    public static UserResponse from(User user, WalletResponse wallet) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), wallet);
    }
}
