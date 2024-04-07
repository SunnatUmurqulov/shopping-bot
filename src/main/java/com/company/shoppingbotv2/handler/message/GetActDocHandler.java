package com.company.shoppingbotv2.handler.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.handler.HandlerHelper;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class GetActDocHandler implements MessageHandler {
    HandlerHelper handlerHelper;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) return;

        String messageText = message.getText();
        if (handlerHelper.getButtonTexts("backToButton").contains(messageText)) {
            handlerHelper.checkDebt(user, true);
        } else if (handlerHelper.getButtonTexts("backToMainMenu").contains(messageText)) {
            handlerHelper.resetMainMenu(user);
        }
    }
}
