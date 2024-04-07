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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.payload.ProductList;
import com.company.shoppingbotv2.payload.ProductListResponse;
import com.company.shoppingbotv2.payload.ProductResponse;
import com.company.shoppingbotv2.repository.OrderProductRepository;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.BasketService;
import com.company.shoppingbotv2.service.UserService;
import com.company.shoppingbotv2.utils.AppConstants;

import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ProductViewHandler implements CallbackQueryHandler {
    ApiClient apiClient;
    HandlerHelper handlerHelper;
    UserService userService;
    TelegramBot telegramBot;
    BasketService basketService;
    OrderProductRepository orderProductRepository;
    MessageSource messageSource;

    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        String callbackQueryData = callbackQuery.getData();
        if (callbackQueryData.startsWith("add_to_cart:")) {
            String[] part = callbackQueryData.split(":");
            String productId = part[1];
            String countString = part[2];
            double count = Double.parseDouble(countString);
            addProductToCart(user, productId, count);
            handlerHelper.deleteCallBackQueryMessage(callbackQuery, user);
            userService.updateUser(user);
            ProductList productList;
            if (!Objects.isNull(user.getSearchKey())) {
                ProductListResponse productListResponse = apiClient.searchProduct(user.getTelegramId(), user.getSearchKey(), user.getCurrentPage());
                productList = new ProductList(productListResponse.products().stream().map(product -> new ProductResponse(product.id(), product.name())).toList(), productListResponse.totalCount());
            } else {
                productList = apiClient.showProductList(user.getTelegramId(), user.getCurrentCategoryId(), user.getCurrentPage());
            }
            int totalPage = productList.totalCount() % AppConstants.PAGE_SIZE == 0
                    ? productList.totalCount() / AppConstants.PAGE_SIZE
                    : productList.totalCount() / AppConstants.PAGE_SIZE + 1;
            handlerHelper.sendProductListToUser(user, productList.productResponseList(), user.getCurrentPage(), totalPage, user.getCurrentCategoryId());
        } else if (callbackQuery.getData().startsWith("increment:")) {
            String[] split = callbackQuery.getData().split(":");
            String productId = split[1];
            incrementProductCount(user, productId, callbackQuery);
            userService.updateUser(user);
        } else if (callbackQuery.getData().startsWith("decrement:")) {
            String[] split = callbackQuery.getData().split(":");
            String productId = split[1];
            String productCount = split[2];
            decrementProductCount(user, productId, productCount, callbackQuery);
            userService.updateUser(user);
        } else if (callbackQuery.getData().startsWith("cancel:")) {
            handlerHelper.deleteCallBackQueryMessage(callbackQuery, user);
            userService.updateUser(user);
            ProductList productList;
            if (!Objects.isNull(user.getSearchKey())) {
                ProductListResponse productListResponse = apiClient.searchProduct(user.getTelegramId(), user.getSearchKey(), user.getCurrentPage());
                productList = new ProductList(productListResponse.products().stream().map(product -> new ProductResponse(product.id(), product.name())).toList(), productListResponse.totalCount());
            } else {
                productList = apiClient.showProductList(user.getTelegramId(), user.getCurrentCategoryId(), user.getCurrentPage());
            }
            int totalPage = productList.totalCount() % AppConstants.PAGE_SIZE == 0
                    ? productList.totalCount() / AppConstants.PAGE_SIZE
                    : productList.totalCount() / AppConstants.PAGE_SIZE + 1;
            handlerHelper.sendProductListToUser(user, productList.productResponseList(), user.getCurrentPage(), totalPage, user.getCurrentCategoryId());
        } else if (callbackQuery.getData().startsWith("counter:")) {
            actionCounter(callbackQuery, user);
        }
    }

    private void actionCounter(CallbackQuery callbackQuery, User user) {
        handlerHelper.deleteCallBackQueryMessage(callbackQuery, user);
        String[] split = callbackQuery.getData().split(":");
        Integer productId = Integer.parseInt(split[1]);
        OrderProduct orderProduct = basketService.getById(productId, user);
        SendMessage infoSendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("enterProductCountInfo", new Object[]{orderProduct.getRemainder()}, user.getLanguage().getLocale()))
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();
//        SendMessage sendMessage = SendMessage.builder()
//                .chatId(user.getTelegramId())
//                .text(handlerHelper.getCounterMessageText(orderProduct, user.getLanguage().getLocale()))
//                .replyMarkup(handlerHelper.getCounterKeyboard(orderProduct, user.getLanguage().getLocale()))
//                .parseMode(ParseMode.HTML)
//                .build();
        telegramBot.sendAnswerMessage(infoSendMessage);
//        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.PRODUCT_COUNTER);
        user.setCurrentOrderProductForCounter(orderProduct.getId());
        userService.updateUser(user);
    }


    private void decrementProductCount(User user, String productId, String productCount, CallbackQuery callbackQuery) {
        OrderProduct orderProduct = basketService.getById(Integer.parseInt(productId), user);
        if (Double.parseDouble(productCount) > 1) {
            orderProduct.setCount(orderProduct.getCount() - 1);
            orderProductRepository.save(orderProduct);
            EditMessageText editMessageText = EditMessageText.builder()
                    .chatId(user.getTelegramId())
                    .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                    .text(messageSource.getMessage("productView", new Object[]{orderProduct.getProductName(), orderProduct.getProductPrice(), orderProduct.getRemainder(), user.getUserTIN()}, user.getLanguage().getLocale()))
                    .replyMarkup(handlerHelper.getProductViewKeyboard(String.valueOf(orderProduct.getId()), orderProduct.getCount(), user))
                    .parseMode(ParseMode.HTML)
                    .build();
            telegramBot.sendEditMessage(editMessageText);
        }
    }

    private void incrementProductCount(User user, String productId, CallbackQuery callbackQuery) {
        OrderProduct orderProduct = basketService.getById(Integer.parseInt(productId), user);
        if (orderProduct.getRemainder() <= orderProduct.getCount()) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(messageSource.getMessage("amountIsLarge", null, user.getLanguage().getLocale()))
                    .showAlert(true)
                    .build());
            return;
        }
        orderProduct.setCount(orderProduct.getCount() + 1);
        orderProductRepository.save(orderProduct);
        EditMessageText editMessageText = EditMessageText.builder()
                .chatId(user.getTelegramId())
                .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                .text(messageSource.getMessage("productView", new Object[]{
                                orderProduct.getProductName(),
                                orderProduct.getProductPrice(),
                                orderProduct.getRemainder(),},
                        user.getLanguage().getLocale()))
                .replyMarkup(handlerHelper.getProductViewKeyboard(String.valueOf(orderProduct.getId()), orderProduct.getCount(), user))
                .parseMode(ParseMode.HTML)
                .build();
        telegramBot.sendEditMessage(editMessageText);
        userService.updateUser(user);
    }

    private void addProductToCart(User user, String productId, double count) {
        OrderProduct orderProduct = basketService.getById(Integer.parseInt(productId), user);
        basketService.addProduct(
                orderProduct.getProductName(),
                orderProduct.getProductPrice(),
                orderProduct.getProductId(),
                orderProduct.getRemainder(),
                count,
                user);
        SendMessage sendMessage = SendMessage.builder()
                .text(messageSource.getMessage("successAddProduct", null, user.getLanguage().getLocale()))
                .chatId(user.getTelegramId())
                .build();
        telegramBot.sendAnswerMessage(sendMessage);
        userService.updateUser(user);

    }
}
