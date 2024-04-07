package com.company.shoppingbotv2.handler.callbackquery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.Contract;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.UserService;

import java.io.File;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class GetActDocCallbackQueryHandler implements CallbackQueryHandler {
    TelegramBot telegramBot;
    MessageSource messageSource;
    UserService userService;
    HandlerHelper helper;
    ApiClient apiClient;

    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        String data = callbackQuery.getData();
        if ("fromDateRange".equals(data)) {
            helper.deleteCallBackQueryMessage(callbackQuery, user);
            sendEnterFromDateRangeMessage(user);
        } else if ("toDateRange".equals(data)) {
            helper.deleteCallBackQueryMessage(callbackQuery, user);
            sendEnterToDateRangeMessage(user);
        } else if ("getActDoc".equals(data)) {
            sendCurrentDocAct(callbackQuery, user);
        }
    }

    private void sendEnterFromDateRangeMessage(User user) {
        SendMessage sendMessage = new SendMessage(
                String.valueOf(user.getTelegramId()),
                messageSource.getMessage("enterFromDateRange", null, user.getLanguage().getLocale())
        );
        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "YYYY-mm-dd"));
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.ENTER_FROM_DATE);
        userService.updateUser(user);
    }

    private void sendEnterToDateRangeMessage(User user) {
        SendMessage sendMessage = new SendMessage(
                String.valueOf(user.getTelegramId()),
                messageSource.getMessage("enterToDateRange", null, user.getLanguage().getLocale())
        );
        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, "YYYY-mm-dd"));
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.ENTER_TO_DATE);
        userService.updateUser(user);
    }

    private void sendCurrentDocAct(CallbackQuery callbackQuery, User user) {
        Message callbackQueryMessage = (Message) callbackQuery.getMessage();
        telegramBot.sendEditMessage(EditMessageText.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("waiting", null, user.getLanguage().getLocale()))
                .messageId(callbackQueryMessage.getMessageId())
                .build()
        );
        Contract currentContract = user.getCurrentContract();
        String currentContractRemoteId = null;
        if (!Objects.isNull(currentContract)) {
            currentContractRemoteId = currentContract.getRemoteId();
        }
        String filePath = apiClient.downloadActDoc(
                user.getTelegramId(),
                currentContractRemoteId,
                user.getFromDateRange(),
                user.getToDateRange()
        );
        telegramBot.sendChatAction(SendChatAction.builder().chatId(user.getTelegramId()).action("upload_document").build());
        File file = new File(filePath);
        SendDocument sendDocument = new SendDocument(String.valueOf(user.getTelegramId()), new InputFile(file));
        telegramBot.sendDocument(sendDocument);
        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), callbackQueryMessage.getMessageId()));
        file.delete();
    }
}
