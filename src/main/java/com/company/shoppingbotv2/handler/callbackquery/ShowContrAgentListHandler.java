package com.company.shoppingbotv2.handler.callbackquery;

import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.exception.SomethingWentWrongException;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.handler.message.CategoryAndProductKeyboardHandler;
import com.company.shoppingbotv2.payload.ContrAgentList;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.UserService;
import com.company.shoppingbotv2.utils.AppConstants;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ShowContrAgentListHandler implements CallbackQueryHandler {
    ApiClient apiClient;
    TelegramBot telegramBot;
    MessageSource messageSource;
    HandlerHelper handlerHelper;
    UserService userService;
    CategoryAndProductKeyboardHandler categoryAndProductKeyboardHandler;

    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        if (callbackQuery.getData().startsWith("page")) {
            handlerContrAgentPagination(user, callbackQuery);
        }
        else if ("stop".equals(callbackQuery.getData())) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(messageSource.getMessage("pageNotFound", null, user.getLanguage().getLocale()))
                    .build()
            );
        } else if (callbackQuery.getData().startsWith("contr_agent")) {
            String[] split = callbackQuery.getData().split(":");
            String contrAgentTIN = split[1];
            user.setUserTIN(contrAgentTIN);
            telegramBot.sendAnswerMessage(SendMessage.builder()
                            .chatId(user.getTelegramId())
                            .text(messageSource.getMessage("success", null, user.getLanguage().getLocale()))
                    .build());
            handlerHelper.deleteCallBackQueryMessage(callbackQuery,user);
            categoryAndProductKeyboardHandler.getCategoryList(user,true, false);
            userService.updateUser(user);
        }
    }

    private void handlerContrAgentPagination(User user, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String[] split = data.split(";");
        if (split.length != 2)
            throw new SomethingWentWrongException("1. Product pagination olishda xatolik callbackQuery: ", callbackQuery);
        int page = Integer.parseInt(split[0].replace("page:", ""));

        if (!split[1].startsWith("name"))
            throw new SomethingWentWrongException("2. Product pagination olishda xatolik callbackQuery: ", callbackQuery);

        String contrAgentName = split[1].replace("name:", "");
        if (contrAgentName.equals("null")) {
            ContrAgentList contrAgentList = apiClient.getContrAgentList(user, page, null);
            int totalPage = contrAgentList.totalCount() % AppConstants.PAGE_SIZE == 0
                    ? contrAgentList.totalCount() / AppConstants.PAGE_SIZE
                    : contrAgentList.totalCount() / AppConstants.PAGE_SIZE + 1;
            telegramBot.sendEditMessage(EditMessageText.builder()
                    .text(messageSource.getMessage("contrAgentList", null, user.getLanguage().getLocale()))
                    .chatId(user.getTelegramId())
                    .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                    .replyMarkup(handlerHelper.getContrAgentsKeyboard(contrAgentList.contrAgents(), page, totalPage, null))
                    .build());
            userService.updateUser(user);
        } else {
            ContrAgentList contrAgentList = apiClient.getContrAgentList(user, page, contrAgentName);
            int totalPage = contrAgentList.totalCount() % AppConstants.PAGE_SIZE == 0
                    ? contrAgentList.totalCount() / AppConstants.PAGE_SIZE
                    : contrAgentList.totalCount() / AppConstants.PAGE_SIZE + 1;
            telegramBot.sendEditMessage(EditMessageText.builder()
                    .text(messageSource.getMessage("contrAgentList", null, user.getLanguage().getLocale()))
                    .chatId(user.getTelegramId())
                    .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                    .replyMarkup(handlerHelper.getContrAgentsKeyboard(contrAgentList.contrAgents(), page, totalPage, contrAgentName))
                    .build());
            userService.updateUser(user);
        }


    }
}
