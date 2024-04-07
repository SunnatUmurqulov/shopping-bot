package com.company.shoppingbotv2.handler.callbackquery;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import com.company.shoppingbotv2.entity.User;

public interface CallbackQueryHandler {
    void handle(CallbackQuery callbackQuery, User user);
}
