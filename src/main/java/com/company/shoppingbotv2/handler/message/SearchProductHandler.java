package com.company.shoppingbotv2.handler.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.payload.ProductListResponse;
import com.company.shoppingbotv2.payload.ProductResponse;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.UserService;
import com.company.shoppingbotv2.utils.AppConstants;

import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class SearchProductHandler implements MessageHandler {
    TelegramBot telegramBot;
    HandlerHelper handlerHelper;
    MessageSource messageSource;
    UserService userService;
    ApiClient apiClient;
    CategoryAndProductKeyboardHandler categoryAndProductKeyboardHandler;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            reenterSearchKey(user);
        }

        String messageText = message.getText();
        if (handlerHelper.getButtonTexts("backToMainMenu").contains(messageText)) {
            user.setCurrentCategoryId(null);
            user.setSearchKey(null);
            user.setCurrentPage(null);
            handlerHelper.resetMainMenu(user);
        } else {
            user.setCurrentPage(1);
            String searchKey = message.getText();
            ProductListResponse productListResponse = apiClient.searchProduct(user.getTelegramId(), searchKey, 1);
            if (Objects.isNull(productListResponse) || Objects.isNull(productListResponse.products())) {
                handleProductNotFound(user);
                return;
            }
            List<ProductResponse> productList = productListResponse.products().stream()
                    .map((product) -> new ProductResponse(product.id(), product.name()))
                    .toList();
            if (productList.isEmpty()) {
                handleProductNotFound(user);
                return;
            }
            int totalPage = productListResponse.totalCount() % AppConstants.PAGE_SIZE == 0
                    ? productListResponse.totalCount() / AppConstants.PAGE_SIZE
                    : productListResponse.totalCount() / AppConstants.PAGE_SIZE + 1;
            handlerHelper.sendProductListToUser(user, productList, 1, totalPage, null);
            user.setSearchKey(searchKey);
            userService.updateUser(user);
        }
    }

    private void handleProductNotFound(User user) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("productNotFoundBySearchKey", null, user.getLanguage().getLocale()))
                .build();
        telegramBot.sendAnswerMessage(sendMessage);
        categoryAndProductKeyboardHandler.getCategoryList(user, false);
    }

    private void reenterSearchKey(User user) {

        String messageText = messageSource.getMessage("incorrectFormat", null, user.getLanguage().getLocale()) + "\n" +
                messageSource.getMessage("enterTheProductName", null, user.getLanguage().getLocale());
        SendMessage sendMessage = new SendMessage(
                String.valueOf(user.getTelegramId()),
                messageText
        );
//        sendMessage.setReplyMarkup(new ForceReplyKeyboard(true, false, messageSource.getMessage("search", null, user.getLanguage().getLocale())));
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.SEARCH_PRODUCT);
        userService.updateUser(user);
    }
}
