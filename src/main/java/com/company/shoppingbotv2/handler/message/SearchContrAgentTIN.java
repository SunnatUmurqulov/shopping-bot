package com.company.shoppingbotv2.handler.message;

import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.exception.SomethingWentWrongException;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.payload.UserTIN;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class SearchContrAgentTIN implements MessageHandler {
    TelegramBot telegramBot;
    MessageSource messageSource;
    SearchContrAgentName searchContrAgentName;
    UserService userService;
    ApiClient apiClient;
    private final HandlerHelper handlerHelper;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            DeleteMessage deleteMessage = new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1);
            telegramBot.sendDeleteMessage(deleteMessage);
            telegramBot.sendAnswerMessage(SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("clickSearchButton", null, user.getLanguage().getLocale()))
                    .build());
            handlerHelper.getContrAgentList(user, null, null);
        } else if (handlerHelper.getButtonTexts("backToContrAgentList").contains(message.getText())) {
            telegramBot.sendDeleteMessage(new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1));
            user.setBotState(BotState.CONTR_AGENT_LIST);
            userService.updateUser(user);
            handlerHelper.getContrAgentList(user, null, null);
        } else {
            String searchKey = message.getText();
            searchContrAgentTIN(user, searchKey);
        }

    }

    private void searchContrAgentTIN(User user, String searchKey) {
        UserTIN contrAgentTIN = apiClient.searchContrAgentTIN(user.getTelegramId(), searchKey);
        if (contrAgentTIN == null) {
            telegramBot.sendAnswerMessage(SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("contr_agent_not_found", null, user.getLanguage().getLocale()))
                    .build());
        } else {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(String.valueOf(contrAgentTIN.contrAgentName()))
                    .callbackData(searchKey)
                    .build();

            SendMessage sendMessage = SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("SelectOrSearch", null, user.getLanguage().getLocale()))
                    .replyMarkup(InlineKeyboardMarkup.builder()
                            .keyboardRow(List.of(button))
                            .build())
                    .build();
            telegramBot.sendAnswerMessage(sendMessage);


            userService.updateUser(user);
        }
    }
}

