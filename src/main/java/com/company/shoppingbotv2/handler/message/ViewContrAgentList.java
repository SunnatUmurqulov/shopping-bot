package com.company.shoppingbotv2.handler.message;

import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ViewContrAgentList implements MessageHandler {
    TelegramBot telegramBot;
    MessageSource messageSource;
    HandlerHelper handlerHelper;
    UserService userService;

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
        }
        String messageText = message.getText();

        if (handlerHelper.getButtonTexts("searchContrAgentName").contains(messageText)) {
            telegramBot.sendDeleteMessage(new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1));
            searchContrAgentName(user);
        }

        else if (handlerHelper.getButtonTexts("searchContrAgentTIN").contains(messageText)) {
            telegramBot.sendDeleteMessage(new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1));
            searchContrAgentTIN(user);
        }

        else if (handlerHelper.getButtonTexts("backToMainMenu").contains(messageText)) {
            telegramBot.sendDeleteMessage(new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 1));
            handlerHelper.resetMainMenu(user);
            userService.updateUser(user);
        }
    }

    private void searchContrAgentTIN(User user) {
        user.setBotState(BotState.ENTER_TIN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(
                new KeyboardButton(messageSource.getMessage("backToContrAgentList", null, user.getLanguage().getLocale()))
        ))));
        replyKeyboardMarkup.setResizeKeyboard(true);

        telegramBot.sendAnswerMessage(SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("enterContrAgentTIN", null, user.getLanguage().getLocale()))
                .replyMarkup(replyKeyboardMarkup)
                .build());
        userService.updateUser(user);
    }

    private void searchContrAgentName(User user) {
        user.setBotState(BotState.ENTER_CONTR_AGENT_NAME_FOR_SEARCH);
        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(
                new KeyboardButton(messageSource.getMessage("backToContrAgentList", null, user.getLanguage().getLocale()))
        ))));
        replyKeyboard.setResizeKeyboard(true);

        telegramBot.sendAnswerMessage(SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("enterContrAgentName", null, user.getLanguage().getLocale()))
                .replyMarkup(replyKeyboard)
                .build());
        userService.updateUser(user);
    }
}
