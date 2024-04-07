package com.company.shoppingbotv2.handler.message;

import org.telegram.telegrambots.meta.api.objects.Message;
import com.company.shoppingbotv2.entity.User;

public interface MessageHandler {
    void handle(Message message, User user);
}
