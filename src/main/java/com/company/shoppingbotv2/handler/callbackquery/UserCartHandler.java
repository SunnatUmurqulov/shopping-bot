package com.company.shoppingbotv2.handler.callbackquery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.entity.enums.OrderProductStatus;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.payload.OrderDocument;
import com.company.shoppingbotv2.payload.OrderItem;
import com.company.shoppingbotv2.repository.OrderProductRepository;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.BasketService;
import com.company.shoppingbotv2.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class UserCartHandler implements CallbackQueryHandler {
    TelegramBot telegramBot;
    MessageSource messageSource;
    HandlerHelper helper;
    UserService userService;
    BasketService basketService;
    OrderProductRepository orderProductRepository;
    ApiClient apiClient;


    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        if (callbackQuery.getData().startsWith("increment")) {
            incrementProductCount(callbackQuery, user);
        } else if (callbackQuery.getData().startsWith("decrement")) {
            decrementProductCount(callbackQuery, user);
        } else if ("place_an_order".equals(callbackQuery.getData())) {
            buyCartProducts(callbackQuery, user);
        } else if ("back_to_main_menu".equals(callbackQuery.getData())) {
            helper.deleteCallBackQueryMessage(callbackQuery, user);
            helper.resetMainMenu(user);
        } else if (callbackQuery.getData().startsWith("cancel")) {
            userService.updateUser(user);
            deleteProduct(callbackQuery, user);
        } else if (callbackQuery.getData().startsWith("counter")) {
            actionCounter(callbackQuery, user);
        }
    }

    private void actionCounter(CallbackQuery callbackQuery, User user) {
        helper.deleteCallBackQueryMessage(callbackQuery, user);
        String[] split = callbackQuery.getData().split(":");
        int productId = Integer.parseInt(split[1]);
        OrderProduct orderProduct = basketService.getProductStatusNew(productId);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("enterProductCountInfo", new Object[]{orderProduct.getRemainder()}, user.getLanguage().getLocale()))
                .build();
//        SendMessage sendMessage = SendMessage.builder().chatId(user.getTelegramId()).text(helper.getCounterMessageText(orderProduct, user.getLanguage().getLocale())).replyMarkup(helper.getCounterKeyboard(orderProduct, user.getLanguage().getLocale())).parseMode(ParseMode.HTML).build();
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.PRODUCT_COUNTER_USER_CART);
        user.setCurrentOrderProductForCounter(orderProduct.getId());
        userService.updateUser(user);
    }

    private void deleteProduct(CallbackQuery callbackQuery, User user) {
        String[] split = callbackQuery.getData().split(":");
        String productId = split[1];
        OrderProduct orderProduct = basketService.getBasketByProduct(productId);
        orderProductRepository.delete(orderProduct);
        Long totalAmount = basketService.getTotalUserCartAmount(user);
        if (totalAmount == null) {
            helper.deleteCallBackQueryMessage(callbackQuery, user);
            helper.resetMainMenu(user);
            userService.updateUser(user);
        } else {
            editUserCard(callbackQuery, user, totalAmount);
        }
    }

    private void editUserCard(CallbackQuery callbackQuery, User user, Long totalAmount) {
        List<OrderProduct> orderProductList = basketService.userShoppingCart(user);
        InlineKeyboardMarkup keyboard = helper.getUserCartKeyboard(user.getLanguage().getLocale(), orderProductList);
        EditMessageText editMessageText = EditMessageText.builder().chatId(user.getTelegramId()).text(messageSource.getMessage("userCart", new Object[]{totalAmount}, user.getLanguage().getLocale())).messageId(((Message) callbackQuery.getMessage()).getMessageId()).replyMarkup(keyboard).build();
        telegramBot.sendEditMessage(editMessageText);
        userService.updateUser(user);
    }

    private void buyCartProducts(CallbackQuery callbackQuery, User user) {
        List<OrderProduct> orderProducts = basketService.userShoppingCart(user);
        List<OrderItem> orderItemList = new ArrayList<>();
        for (OrderProduct orderProduct : orderProducts) {
            orderItemList.add(new OrderItem(orderProduct.getProductId(), String.valueOf(orderProduct.getCount()), String.valueOf(orderProduct.getProductPrice())));
        }
        userService.updateUser(user);
        helper.deleteCallBackQueryMessage(callbackQuery, user);
        Message waitingMessage = helper.setWaitingMessage(user);
        OrderDocument orderDocument = apiClient.orderByUser(user.getTelegramId(), user.getUserTIN(), orderItemList);
        if (orderDocument == null) {
            telegramBot.sendAnswerMessage(SendMessage.builder().chatId(user.getTelegramId()).text(messageSource.getMessage("errorMessageOrderDocument", null, user.getLanguage().getLocale())).build());
            helper.resetMainMenu(user);
            userService.updateUser(user);
            return;
        }

        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setStatus(OrderProductStatus.ORDERED);
            orderProduct.setContrAgentTIN(user.getUserTIN());
            orderProductRepository.save(orderProduct);
        }
        user.setUserTIN(null);
        telegramBot.sendAnswerMessage(SendMessage.builder().chatId(user.getTelegramId()).text(messageSource.getMessage("apiDataSent", new Object[]{orderDocument.documentId(), orderDocument.documentStatus(), orderDocument.documentDate()}, user.getLanguage().getLocale())).build());
        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
        helper.resetMainMenu(user);
        userService.updateUser(user);

    }

    private void decrementProductCount(CallbackQuery callbackQuery, User user) {
        String[] split = callbackQuery.getData().split(":");
        String productId = split[1];
        OrderProduct orderProduct = basketService.getBasketByProduct(productId);
        if (orderProduct.getCount() > 1) {
            basketService.updateBasketCount(orderProduct, orderProduct.getCount() - 1);
            sendUserCart(callbackQuery, user);
        }

    }

    private void incrementProductCount(CallbackQuery callbackQuery, User user) {
        String[] split = callbackQuery.getData().split(":");
        String productId = split[1];
        OrderProduct orderProduct = basketService.getBasketByProduct(productId);
        if (orderProduct.getRemainder() <= orderProduct.getCount()) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.getId()).text(messageSource.getMessage("amountIsLarge", null, user.getLanguage().getLocale())).showAlert(true).build());
            return;
        }
        basketService.updateBasketCount(orderProduct, orderProduct.getCount() + 1);
        sendUserCart(callbackQuery, user);
    }

    private void sendUserCart(CallbackQuery callbackQuery, User user) {
        Long totalAmount = basketService.getTotalUserCartAmount(user);
        editUserCard(callbackQuery, user, totalAmount);
    }
}
