package com.company.shoppingbotv2.handler.callbackquery;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.repository.OrderProductRepository;
import com.company.shoppingbotv2.service.BasketService;
import com.company.shoppingbotv2.service.UserService;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class ProductCounterHandler implements CallbackQueryHandler {
    HandlerHelper helper;
    BasketService basketService;
    TelegramBot telegramBot;
    OrderProductRepository orderProductRepository;
    MessageSource messageSource;
    ShowProductListHandler showProductListHandler;
    UserService userService;

    @Override
    public void handle(CallbackQuery callbackQuery, User user) {
        String callbackQueryData = callbackQuery.getData();
        String[] parts = callbackQueryData.split(":");
        String command = parts[1];
        int productId = Integer.parseInt(parts[2]);
        OrderProduct orderProduct = basketService.getById(productId, user);
        switch (command) {
            case "confirm" -> handleConfirmCommand(callbackQuery, orderProduct, user);
            case "clear" -> handleClearCommand(callbackQuery, orderProduct, user);
            case "remove" -> handleRemoveCommand(callbackQuery, orderProduct, user);
            default -> handleNumberButtonCommand(callbackQuery, orderProduct, user);
        }
    }

    private void handleNumberButtonCommand(CallbackQuery callbackQuery, OrderProduct orderProduct, User user) {
        String[] parts = callbackQuery.getData().split(":");
        String number = parts[1];
        if (orderProduct.getCount() == 0) {
            orderProduct.setCount(Double.parseDouble(number));
        } else {
            orderProduct.setCount(Integer.parseInt(orderProduct.getCount() + number));
        }
        if (orderProduct.getCount() >= Math.pow(10, 6)) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(messageSource.getMessage("countOfProductTooLarge", null, user.getLanguage().getLocale()))
                    .showAlert(true)
                    .build());
            return;
        }
        if (orderProduct.getRemainder() < orderProduct.getCount()) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(messageSource.getMessage("amountIsLarge", null, user.getLanguage().getLocale()))
                    .showAlert(true)
                    .build());
            return;
        }
        sendEditMessage(callbackQuery, orderProduct, user);
    }

    private void handleRemoveCommand(CallbackQuery callbackQuery, OrderProduct orderProduct, User user) {
        String count = String.valueOf(orderProduct.getCount());
        if (count.length() == 1) {
            count = "0";
        } else {
            count = count.substring(0, count.length() - 1);
        }
        orderProduct.setCount(Integer.parseInt(count));
        sendEditMessage(callbackQuery, orderProduct, user);
    }

    private void handleClearCommand(CallbackQuery callbackQuery, OrderProduct orderProduct, User user) {
        orderProduct.setCount(0);
        sendEditMessage(callbackQuery, orderProduct, user);
    }

    private void handleConfirmCommand(CallbackQuery callbackQuery, OrderProduct orderProduct, User user) {
        if (orderProduct.getCount() == 0) {
            orderProduct.setCount(1);
        }
        if (orderProduct.getRemainder() < orderProduct.getCount()) {
            telegramBot.sendAnswerCallbackQuery(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQuery.getId())
                    .text(messageSource.getMessage("amountIsLarge", null, user.getLanguage().getLocale()))
                    .showAlert(true)
                    .build());
            return;
        }
        user.setBotState(BotState.PRODUCT_VIEW);
        userService.updateUser(user);
        orderProduct = orderProductRepository.save(orderProduct);
        showProductListHandler.sendProductView(orderProduct, user);
        helper.deleteCallBackQueryMessage(callbackQuery, user);
    }

    private void sendEditMessage(CallbackQuery callbackQuery, OrderProduct orderProduct, User user) {
        orderProduct.setAmount((long) orderProduct.getCount() * orderProduct.getProductPrice());
        telegramBot.sendEditMessage(EditMessageText.builder()
                .chatId(user.getTelegramId())
                .messageId(((Message) callbackQuery.getMessage()).getMessageId())
                .text(helper.getCounterMessageText(orderProduct, user.getLanguage().getLocale()))
                .parseMode(ParseMode.HTML)
                .replyMarkup(helper.getCounterKeyboard(orderProduct, user.getLanguage().getLocale()))
                .build());
        orderProductRepository.save(orderProduct);
    }
}
