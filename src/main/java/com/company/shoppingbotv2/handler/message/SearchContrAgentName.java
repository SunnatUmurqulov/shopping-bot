package com.company.shoppingbotv2.handler.message;

import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class SearchContrAgentName implements MessageHandler {
    HandlerHelper handlerHelper;
    TelegramBot telegramBot;
    MessageSource messageSource;
    UserService userService;
    ApiClient apiClient;

    @Override
    public void handle(Message message, User user) {
        String messageText = message.getText();

        if (!message.hasText()) {
            DeleteMessage deleteMessage = new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1);
            telegramBot.sendDeleteMessage(deleteMessage);
            telegramBot.sendAnswerMessage(SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("clickSearchButton", null, user.getLanguage().getLocale()))
                    .build());
            handlerHelper.getContrAgentList(user, null, null);
        }

        else if (handlerHelper.getButtonTexts("backToContrAgentList").contains(messageText)) {
            telegramBot.sendDeleteMessage(new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1));
            user.setBotState(BotState.CONTR_AGENT_LIST);
            userService.updateUser(user);
            handlerHelper.getContrAgentList(user, null, null);
        }

        else {
            telegramBot.sendDeleteMessage(new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1));
            String searchKey = message.getText();
            handlerHelper.getContrAgentList(user, null, searchKey);
            user.setBotState(BotState.CONTR_AGENT_LIST);
            userService.updateUser(user);
        }

    }

}
