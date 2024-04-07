package com.company.shoppingbotv2.handler.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.UserService;

import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class EnterFromDateHandler implements MessageHandler {
    TelegramBot telegramBot;
    HandlerHelper handlerHelper;
    MessageSource messageSource;
    UserService userService;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            reenterFromDate("incorrectFormat", user);
        }
        String toDate = user.getToDateRange();
        String fromDate = message.getText();
        String errorCode = handlerHelper.validateDateRange(fromDate, toDate);
        if (!Objects.isNull(errorCode)) {
            reenterFromDate(errorCode, user);
            return;
        }
        user.setFromDateRange(fromDate);
        handlerHelper.sendGetActDocMessage(user);
    }

    private void reenterFromDate(String errorCode, User user) {
        String messageText = messageSource.getMessage(errorCode, null, user.getLanguage().getLocale()) + "\n" +
                messageSource.getMessage("enterFromDateRange", null, user.getLanguage().getLocale());
        SendMessage sendMessage = new SendMessage(
                String.valueOf(user.getTelegramId()),
                messageText
        );
        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "YYYY-mm-dd"));
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.ENTER_FROM_DATE);
        userService.updateUser(user);
    }


}
