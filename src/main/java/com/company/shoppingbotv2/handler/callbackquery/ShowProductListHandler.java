package com.company.shoppingbotv2.handler.callbackquery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.exception.SomethingWentWrongException;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.handler.message.CategoryAndProductKeyboardHandler;
import com.company.shoppingbotv2.payload.GetProduct;
import com.company.shoppingbotv2.payload.ProductList;
import com.company.shoppingbotv2.payload.ProductListResponse;
import com.company.shoppingbotv2.payload.ProductResponse;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.BasketService;
import com.company.shoppingbotv2.service.UserService;
import com.company.shoppingbotv2.utils.AppConstants;

import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ShowProductListHandler implements CallbackQueryHandler {
    ApiClient apiClient;
    MessageSource messageSource;
    TelegramBot telegramBot;
    HandlerHelper handlerHelper;
    UserService userService;
    BasketService basketService;
    CategoryAndProductKeyboardHandler categoryAndProductKeyboardHandler;

    public void handle(CallbackQuery callbackQuery, User user) {
        if ("backCategory".equals(callbackQuery.getData())) {
            handlerHelper.deleteCallBackQueryMessage(callbackQuery, user);
            categoryAndProductKeyboardHandler.getCategoryList(user, false);
        } else if (callbackQuery.getData().startsWith("page")) {
            handleProductPagination(callbackQuery, user);
        } else if ("stop".equals(callbackQuery.getData())) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(messageSource.getMessage("pageNotFound", null, user.getLanguage().getLocale()))
                    .build()
            );
        } else if (callbackQuery.getData().startsWith("page")) {
            actionProductView(callbackQuery, user);
        } else if (callbackQuery.getData().startsWith("product")) {
            actionProductView(callbackQuery, user);
        }
    }

    private void handleProductPagination(CallbackQuery callbackQuery, User user) {
        String data = callbackQuery.getData();
        String[] split = data.split(";");
        if (split.length != 2)
            throw new SomethingWentWrongException("1. Product pagination olishda xatolik callbackQuery: ", callbackQuery);
        int page = Integer.parseInt(split[0].replace("page:", ""));
        if (!split[1].startsWith("category"))
            throw new SomethingWentWrongException("2. Product pagination olishda xatolik callbackQuery: ", callbackQuery);
        String categoryId = split[1].replace("category:", "");
        ProductList productList;
        if (!Objects.isNull(user.getSearchKey())) {
            ProductListResponse productListResponse = apiClient.searchProduct(user.getTelegramId(), user.getSearchKey(), page);
            productList = new ProductList(productListResponse.products().stream().map(product -> new ProductResponse(product.id(), product.name())).toList(), productListResponse.totalCount());
        } else {
            productList = apiClient.showProductList(user.getTelegramId(), categoryId, page);
        }
        int totalPage = productList.totalCount() % AppConstants.PAGE_SIZE == 0
                ? productList.totalCount() / AppConstants.PAGE_SIZE
                : productList.totalCount() / AppConstants.PAGE_SIZE + 1;
        telegramBot.sendEditMessage(EditMessageText.builder()
                .text(messageSource.getMessage("showProductList", null, user.getLanguage().getLocale()))
                .chatId(user.getTelegramId())
                .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                .replyMarkup(handlerHelper.getProductsKeyboard(categoryId, productList.productResponseList(), page, totalPage))
                .build());
        user.setCurrentPage(page);
        userService.updateUser(user);
    }

    private void actionProductView(CallbackQuery callbackQuery, User user) {
        String[] split = callbackQuery.getData().split(":");
        String productId = split[1];
        user.setBotState(BotState.PRODUCT_VIEW);
        GetProduct product = apiClient.getProduct(user.getTelegramId(), productId);
        OrderProduct savedProduct = basketService.createTemporaryOrder(user, product);
        sendProductView(savedProduct, user);
        handlerHelper.deleteCallBackQueryMessage(callbackQuery, user);
        userService.updateUser(user);
    }

    public void sendProductView(OrderProduct product, User user) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("productView", new Object[]{product.getProductName(), product.getProductPrice(), product.getRemainder()}, user.getLanguage().getLocale()))
                .replyMarkup(handlerHelper.getProductViewKeyboard(String.valueOf(product.getId()), product.getCount(), user))
                .parseMode(ParseMode.HTML)
                .build();
        telegramBot.sendAnswerMessage(sendMessage);
    }
}
