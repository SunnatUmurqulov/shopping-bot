package com.company.shoppingbotv2.handler.callbackquery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.service.BasketService;
import com.company.shoppingbotv2.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class MyOrdersHandler implements CallbackQueryHandler {
    TelegramBot telegramBot;
    MessageSource messageSource;
    HandlerHelper helper;
    UserService userService;
    BasketService basketService;


    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        if ("cancel".equals(callbackQuery.getData())) {
            helper.deleteCallBackQueryMessage(callbackQuery, user);
            helper.resetMainMenu(user);
        } else if ("stop".equals(callbackQuery.getData())) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(messageSource.getMessage("pageNotFound", null, user.getLanguage().getLocale()))
                    .build()
            );
        } else if (callbackQuery.getData().startsWith("page")) {
            ordersPage(callbackQuery, user);
        } else if (callbackQuery.getData().startsWith("product")) {
            helper.deleteCallBackQueryMessage(callbackQuery,user);
            actionGetProductInfo(callbackQuery, user);
        }else if (callbackQuery.getData().equals("back_to_main_menu")){
            helper.deleteCallBackQueryMessage(callbackQuery,user);
            helper.resetMainMenu(user);
        } else if (callbackQuery.getData().equals("back")) {
            helper.deleteCallBackQueryMessage(callbackQuery,user);
            backToOrderProductList(callbackQuery,user);
        }

    }

    private void backToOrderProductList(CallbackQuery callbackQuery, User user) {
        int pageSize = 10;
        PageRequest pageable = PageRequest.of(user.getCurrentPage(), pageSize);
        Page<OrderProduct> orderProducts = basketService.userOrders(user, pageable);
        int totalPage = orderProducts.getTotalPages() % pageSize == 0 ? orderProducts.getTotalPages() / pageSize : orderProducts.getTotalPages() / pageSize + 1;
        telegramBot.sendAnswerMessage(SendMessage.builder()
                .text(messageSource.getMessage("showProductList", null, user.getLanguage().getLocale()))
                .chatId(user.getTelegramId())
                .replyMarkup(getProductsKeyboard(orderProducts, user.getCurrentPage(), totalPage, user.getLanguage().getLocale()))
                .build());
        userService.updateUser(user);
    }

    private void actionGetProductInfo(CallbackQuery callbackQuery, User user) {
        String[] split = callbackQuery.getData().split(":");
        int productId = Integer.parseInt(split[1]);
        OrderProduct productStatusOrder = basketService.getProductStatusOrder(user, productId);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("orderProductView", new Object[]{
                                productStatusOrder.getProductName(),
                                productStatusOrder.getProductPrice(),
                                productStatusOrder.getCount(),
                                productStatusOrder.getAmount()},
                        user.getLanguage().getLocale()))
                .replyMarkup(buildProductViewKeyboard(user))
                .parseMode(ParseMode.HTML)
                .build();
        telegramBot.sendAnswerMessage(sendMessage);
        userService.updateUser(user);
    }

    private ReplyKeyboard buildProductViewKeyboard(User user) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text(messageSource.getMessage("backToMainMenu", null, user.getLanguage().getLocale()))
                        .callbackData("back_to_main_menu")
                        .build(),
                InlineKeyboardButton.builder()
                        .text(messageSource.getMessage("Cancel", null, user.getLanguage().getLocale()))
                        .callbackData("back")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    private void ordersPage(CallbackQuery callbackQuery, User user) {
        int pageSize = 10;
        String[] split = callbackQuery.getData().split(":");
        int page = Integer.parseInt(split[1]);
        PageRequest pageable = PageRequest.of(page, pageSize);
        Page<OrderProduct> orderProducts = basketService.userOrders(user, pageable);
        user.setCurrentPage(page);
        telegramBot.sendEditMessage(EditMessageText.builder()
                .text(messageSource.getMessage("showProductList", null, user.getLanguage().getLocale()))
                .chatId(user.getTelegramId())
                .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                .replyMarkup(getProductsKeyboard(orderProducts, page, orderProducts.getTotalPages(), user.getLanguage().getLocale()))
                .build());
        userService.updateUser(user);

    }


    public void sendOrderListToUser(User user) {
        int currentPage = 0;
        int pageSize = 10;
        PageRequest pageable = PageRequest.of(currentPage, pageSize);
        Page<OrderProduct> orderProducts = basketService.userOrders(user, pageable);
        SendMessage sendMessage = SendMessage.builder()
                .text(messageSource.getMessage("showProductList", null, user.getLanguage().getLocale()))
                .chatId(user.getTelegramId())
                .build();
        sendMessage.setReplyMarkup(getProductsKeyboard(orderProducts, currentPage, orderProducts.getTotalPages(), user.getLanguage().getLocale()));

        telegramBot.sendAnswerMessage(sendMessage);
        user.setCurrentPage(currentPage);
        userService.updateUser(user);

    }

    private InlineKeyboardMarkup getProductsKeyboard(Page<OrderProduct> orderProducts, int currentPage, int totalPage, Locale locale) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (OrderProduct orderProduct : orderProducts) {
            keyboard.add(List.of(InlineKeyboardButton.builder().text(orderProduct.getProductName()).callbackData("product:" + orderProduct.getId()).build()));
        }
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder()
                .text(messageSource.getMessage("backToMainMenu", null, locale))
                .callbackData("cancel")
                .build();
        InlineKeyboardButton nextButton;
        if (totalPage > 1) {
            InlineKeyboardButton backButton;
            if (currentPage == 0) {
                backButton = InlineKeyboardButton.builder()
                        .text("⏹")
                        .callbackData("stop")
                        .build();
            } else {
                backButton = InlineKeyboardButton.builder()
                        .text("⬅️")
                        .callbackData(String.format("page:%s", (currentPage - 1)))
                        .build();
            }
            if (currentPage == (totalPage - 1)) {
                nextButton = InlineKeyboardButton.builder()
                        .text("⏹")
                        .callbackData("stop")
                        .build();
            } else {
                nextButton = InlineKeyboardButton.builder()
                        .text("➡️")
                        .callbackData(String.format("page:%s", (currentPage + 1)))
                        .build();
            }
            keyboard.add(List.of(backButton, cancelButton, nextButton));
        } else {
            keyboard.add(List.of(cancelButton));
        }

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}