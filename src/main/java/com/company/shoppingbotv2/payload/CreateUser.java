package com.company.shoppingbotv2.payload;

public record CreateUser(
        long telegramId,
        String firstName,
        String lastName,
        String username
) {
}
