package com.company.shoppingbotv2.utils;

public interface AppConstants {
    String X_API_TELEGRAM_BOT_KEY_HEADER = "X-Telegram-Bot-Api-Secret-Token";
    String BASE_UPDATE_HANDLER_URI = "/update";
    String ADMIN_TELEGRAM_ID = "-1002052794029";
    String TELEGRAM_BOT_START_MESSAGE = """
            🇺🇿 - Tilni tanlang
            🇷🇺 – Выберите язык
            🇬🇧 – Select language
            """;
    String API_KEY_PREFIX = "Basic ";
    short PAGE_SIZE = 10;
}
