package com.company.shoppingbotv2.handler.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.entity.enums.UserStatus;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.UserService;

import java.util.List;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class SetContactHandler implements MessageHandler {
    TelegramBot telegramBot;
    UserService userService;
    MessageSource messageSource;
    HandlerHelper handlerHelper;
    ApiClient apiClient;

    @Override
    public void handle(Message message, User user) {
        if (!(message.hasContact()
                && user.getTelegramId().equals(message.getContact().getUserId()))) {
            actionRestartContact(user);
            return;
        }
        Message waitingMessage = handlerHelper.setWaitingMessage(user);
        String phoneNumber = message.getContact().getPhoneNumber();
        user.setPhoneNumber(phoneNumber);
        Map<String, String> response = apiClient.getCustomerName(user.getLanguage(), phoneNumber, user.getTelegramId());
        if (!response.containsKey("customer_name")) {
            String messageText = messageSource.getMessage("customerNotFound", null, user.getLanguage().getLocale());
            if (response.containsKey("error_text")) {
                messageText = !response.get("error_text").isBlank() ? response.get("error_text") : messageText;
            }
            SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
            telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
            telegramBot.sendAnswerMessage(sendMessage);
            return;
        }
        String messageText = messageSource.getMessage("successfullyRegistered", new Object[]{response.get("customer_name")}, user.getLanguage().getLocale());
        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
        sendMessage.setReplyMarkup(handlerHelper.getMainMenuKeyboard(user.getLanguage().getLocale()));
        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
        telegramBot.sendAnswerMessage(sendMessage);
        user.setFirstInit(false);
        user.setBotState(BotState.MAIN_MENU);
        user.setStatus(UserStatus.ACTIVE);
        userService.updateUser(user);

    }

    private void actionRestartContact(User user) {
        KeyboardButton button = new KeyboardButton(messageSource.getMessage("sendPhoneNumber", null, user.getLanguage().getLocale()));
        button.setRequestContact(true);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(button))));
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setResizeKeyboard(true);

        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("shareContactToContinue", null, user.getLanguage().getLocale()))
                .replyMarkup(keyboardMarkup)
                .build();
        telegramBot.sendAnswerMessage(sendMessage);
    }
}
