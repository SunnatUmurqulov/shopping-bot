package com.company.shoppingbotv2.handler.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.Contract;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.UserService;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ViewContractHandler implements MessageHandler {
    TelegramBot telegramBot;
    HandlerHelper handlerHelper;
    MessageSource messageSource;
    UserService userService;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            sendFailedMessage(user);
            return;
        }

        String messageText = message.getText();
        if (handlerHelper.getButtonTexts("getContractDocButton").contains(messageText)) {
            sendGetActDocMessage(user);
        } else if (handlerHelper.getButtonTexts("backToButton").contains(messageText)) {
            handlerHelper.checkDebt(user, true);
        } else if (handlerHelper.getButtonTexts("backToMainMenu").contains(messageText)) {
            handlerHelper.resetMainMenu(user);
        } else {
            sendFailedMessage(user);
        }
    }

    private void sendFailedMessage(User user) {
        String messageText = messageSource.getMessage("plsPressKeyboard", null, user.getLanguage().getLocale());
        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()), messageText);
        sendMessage.setReplyMarkup(getContractViewKeyboard(user.getLanguage().getLocale()));
        telegramBot.sendAnswerMessage(sendMessage);
    }

    private void sendGetActDocMessage(User user) {
        Contract currentContract = user.getCurrentContract();
        String currentContractName;
        if (Objects.isNull(currentContract)) {
            currentContractName = messageSource.getMessage("allContracts", null, user.getLanguage().getLocale());
        } else {
            currentContractName = currentContract.getName();
        }
        String fromDateRange = user.getFromDateRange();
        if (Objects.isNull(fromDateRange)) {
            fromDateRange = "-";
        }
        String toDateRange = user.getToDateRange();
        if (Objects.isNull(toDateRange)) {
            toDateRange = "-";
        }
        SendMessage mockMessage = new SendMessage(String.valueOf(user.getTelegramId()), ".");
        ReplyKeyboardMarkup mockMessageKeyboard = new ReplyKeyboardMarkup(
                List.of(new KeyboardRow(List.of(new KeyboardButton(
                                messageSource.getMessage("backToButton", null, user.getLanguage().getLocale())
                        ))),
                        new KeyboardRow(List.of(new KeyboardButton(
                                messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale())
                        )))
                ));
        mockMessageKeyboard.setResizeKeyboard(true);
        mockMessage.setReplyMarkup(mockMessageKeyboard);
        SendMessage sendMessage = new SendMessage(
                String.valueOf(user.getTelegramId()),
                messageSource.getMessage(
                        "getActDoc",
                        new Object[]{currentContractName, fromDateRange, toDateRange},
                        user.getLanguage().getLocale()
                ));
        sendMessage.setReplyMarkup(
                new InlineKeyboardMarkup(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("fromDateRangeButton", null, user.getLanguage().getLocale()))
                                .callbackData("fromDateRange")
                                .build()
                        ),
                        List.of(InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("toDateRangeButton", null, user.getLanguage().getLocale()))
                                .callbackData("toDateRange")
                                .build()
                        ),
                        List.of(InlineKeyboardButton.builder()
                                .text(messageSource.getMessage("getActDocButton", null, user.getLanguage().getLocale()))
                                .callbackData("getActDoc")
                                .build()
                        )
                )));
        telegramBot.sendAnswerMessage(mockMessage);
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.GET_ACT_DOC);
        userService.updateUser(user);
    }

    public ReplyKeyboard getContractViewKeyboard(Locale locale) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(
                new KeyboardRow(List.of(new KeyboardButton(messageSource.getMessage("getContractDocButton", null, locale)))),
                new KeyboardRow(List.of(
                        new KeyboardButton(messageSource.getMessage("backToButton", null, locale)),
                        new KeyboardButton(messageSource.getMessage("backToMainMenu", null, locale))
                ))
        ));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }
}
