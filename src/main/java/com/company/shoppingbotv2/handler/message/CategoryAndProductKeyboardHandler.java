package com.company.shoppingbotv2.handler.message;

import com.company.shoppingbotv2.entity.enums.OrderProductStatus;
import com.company.shoppingbotv2.payload.CategoryResponse;
import com.company.shoppingbotv2.repository.OrderProductRepository;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.payload.CategoryList;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class CategoryAndProductKeyboardHandler implements MessageHandler {
    HandlerHelper handlerHelper;
    MessageSource messageSource;
    TelegramBot telegramBot;
    UserService userService;
    ApiClient apiClient;
    private final OrderProductRepository orderProductRepository;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            getCategoryList(user, true);
        }
        String messageText = message.getText();
        if (handlerHelper.getButtonTexts("searchButton").contains(messageText)) {
            telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), message.getMessageId() - 1));
            searchProducts(user);
        } else if (handlerHelper.getButtonTexts("backToMainMenu").contains(messageText)) {
            telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), message.getMessageId() - 1));
            resetMainMenu(user);
        } else {
            telegramBot.sendDeleteMessage(new DeleteMessage(user.getTelegramId().toString(), message.getMessageId() - 2));
            getCategoryList(user, true);
        }
    }

    private void resetMainMenu(User user) {
        user.setCurrentCategoryId(null);
        user.setSearchKey(null);
        user.setCurrentPage(null);
        SendMessage sendMessage = new SendMessage(String.valueOf(user.getTelegramId()),
                messageSource.getMessage(
                        "mainMenu",
                        null,
                        user.getLanguage().getLocale()));
        sendMessage.setReplyMarkup(handlerHelper.getMainMenuKeyboard(user.getLanguage().getLocale()));
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.MAIN_MENU);
        userService.updateUser(user);
    }

    private void searchProducts(User user) {
        telegramBot.sendAnswerMessage(SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("enterTheProductName", null, user.getLanguage().getLocale()))
                .build());
        user.setBotState(BotState.SEARCH_PRODUCT);
        userService.updateUser(user);
    }

    public void getCategoryList(User user, boolean setKeyboard, boolean showContrAgentList) {
        if (showContrAgentList && (!orderProductRepository.existsOrderProductByUserIdAndStatus(user.getId(), OrderProductStatus.NEW) || Objects.isNull(user.getUserTIN()))) {
            user.setBotState(BotState.CONTR_AGENT_LIST);
            userService.updateUser(user);
            handlerHelper.getContrAgentList(user, null, null);
            return;
        }
        user.setCurrentCategoryId(null);
        user.setSearchKey(null);
        user.setCurrentPage(null);
        user.setBotState(BotState.CATEGORY_LIST);
        CategoryList categories = apiClient.getCategories(user.getTelegramId(), user.getUserTIN());

        if (categories.categoryResponses() == null) {
            telegramBot.sendAnswerMessage(SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("categoryNotFound", null, user.getLanguage().getLocale()))
                    .build());
            resetMainMenu(user);
            userService.updateUser(user);
            return;
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < categories.categoryResponses().size(); i += 2) {
            if (categories.categoryResponses().size() == i + 1) {
                rows.add(List.of(
                        InlineKeyboardButton.builder().text(categories.categoryResponses().get(i).name()).callbackData("category:" + categories.categoryResponses().get(i).id()).build()
                ));
            } else {
                rows.add(List.of(
                        InlineKeyboardButton.builder().text(categories.categoryResponses().get(i).name()).callbackData("category:" + categories.categoryResponses().get(i).id()).build(),
                        InlineKeyboardButton.builder().text(categories.categoryResponses().get(i + 1).name()).callbackData("category:" + categories.categoryResponses().get(i + 1).id()).build()
                ));
            }
        }

        ReplyKeyboardMarkup replyKeyboard = new ReplyKeyboardMarkup(List.of(new KeyboardRow(List.of(
                new KeyboardButton(messageSource.getMessage("searchButton", null, user.getLanguage().getLocale())),
                new KeyboardButton(messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale()))
        ))));

        replyKeyboard.setResizeKeyboard(true);
        if (setKeyboard) {
            telegramBot.sendAnswerMessage(SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("collectProducts", null, user.getLanguage().getLocale()))
                    .replyMarkup(replyKeyboard)
                    .build());
        }
        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder().clearKeyboard().keyboard(rows).build();
        telegramBot.sendAnswerMessage(SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("categoryList", null, user.getLanguage().getLocale()))
                .replyMarkup(keyboard)
                .build()
        );

        userService.updateUser(user);
    }

    public void getCategoryList(User user, boolean setKeyboard) {
        getCategoryList(user, setKeyboard, true);
    }
}
