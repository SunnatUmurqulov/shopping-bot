package com.company.shoppingbotv2.handler.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.company.shoppingbotv2.config.DataLoader;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.entity.enums.Language;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.UserService;

import java.util.List;
import java.util.Objects;

@Service

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ChooseLanguageHandler implements MessageHandler {
    TelegramBot telegramBot;
    HandlerHelper handlerHelper;
    UserService userService;
    MessageSource messageSource;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            handlerHelper.startCommand(user);
            return;
        }
        Language language = Language.getLanguageByText(message.getText());
        if (Objects.isNull(language)) {
            handlerHelper.startCommand(user);
            return;
        }
        user.setLanguage(language);
        SendMessage sendMessage;
        if (user.isFirstInit()) {
            String comment = DataLoader.starterComment.get(user.getLanguage());
            String bundleMessage = messageSource.getMessage("sharePhoneNumber", null, language.getLocale());
            String messageText = String.format("%s\n%s", comment, bundleMessage);
            sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
            KeyboardButton button = new KeyboardButton(messageSource.getMessage("sendPhoneNumber", null, language.getLocale()));
            button.setRequestContact(true);
            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(button))));
            keyboardMarkup.setOneTimeKeyboard(true);
            keyboardMarkup.setResizeKeyboard(true);
            sendMessage.setReplyMarkup(keyboardMarkup);
            user.setBotState(BotState.SEND_PHONE_NUMBER);
        } else {
            user.setBotState(BotState.MAIN_MENU);
            sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageSource.getMessage("languageUpdatedSuccessfully", null, user.getLanguage().getLocale()));
            sendMessage.setReplyMarkup(handlerHelper.getMainMenuKeyboard(user.getLanguage().getLocale()));
        }
        telegramBot.sendAnswerMessage(sendMessage);
        userService.updateUser(user);
    }
}
