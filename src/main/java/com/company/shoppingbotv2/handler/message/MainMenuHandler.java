package com.company.shoppingbotv2.handler.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import com.company.shoppingbotv2.config.DataLoader;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.Contract;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.handler.HandlerHelper;
import com.company.shoppingbotv2.handler.callbackquery.MyOrdersHandler;
import com.company.shoppingbotv2.payload.DebtCheckResponse;
import com.company.shoppingbotv2.service.ApiClient;
import com.company.shoppingbotv2.service.BasketService;
import com.company.shoppingbotv2.service.ContractService;
import com.company.shoppingbotv2.service.UserService;

import java.util.List;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class MainMenuHandler implements MessageHandler {
    TelegramBot telegramBot;
    HandlerHelper handlerHelper;
    MessageSource messageSource;
    CategoryAndProductKeyboardHandler categoryAndProductKeyboardHandler;
    BasketService basketService;
    UserService userService;
    MyOrdersHandler myOrdersHandler;
    ApiClient apiClient;
    ContractService contractService;


    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            handlerHelper.resetMainMenu(user);
            return;
        }
        String messageText = message.getText();
         if (handlerHelper.getButtonTexts("categoryListButton").contains(messageText)) {
            categoryAndProductKeyboardHandler.getCategoryList(user, true);
        } else if (handlerHelper.getButtonTexts("myOrdersButton").contains(messageText)) {
            actionMyOrderButton(user);
        } else if (handlerHelper.getButtonTexts("basketButton").contains(messageText)) {
            telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), message.getMessageId() - 1));
            shoppingCart(user, false);
        } else if (handlerHelper.getButtonTexts("contactUsButton").contains(messageText)) {
            actionContactUs(user);
        } else if (handlerHelper.getButtonTexts("changeLanguageButton").contains(messageText)) {
            handlerHelper.sendChooseLanguage(user);
        }else {
            handlerHelper.resetMainMenu(user);
        }
    }

    private void checkDebt(User user) {
        Message waitingMessage = handlerHelper.setWaitingMessage(user);
        DebtCheckResponse response = apiClient.getDebtContracts(user.getTelegramId());
        if (Objects.isNull(response.totalSum()) || response.totalSum().equals(0D)) {
            telegramBot.sendAnswerMessage(SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("youHaveNotDebts", null, user.getLanguage().getLocale()))
                    .build());
            handlerHelper.resetMainMenu(user);
            return;
        }
        List<Contract> contracts = contractService.saveUserContracts(user, response);
        String messageText = handlerHelper.buildAllContractMessage(user, contracts);
        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), waitingMessage.getMessageId()));
        telegramBot.sendAnswerMessage(new SendMessage(String.valueOf(user.getTelegramId()), messageText));
        handlerHelper.resetMainMenu(user);
        userService.updateUser(user);
    }

    private void actionMyOrderButton(User user) {
        boolean existsProductOrder = basketService.orderProductByUserId(user.getId());
        if (!existsProductOrder){
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("yourCartEmpty", null, user.getLanguage().getLocale()))
                    .replyMarkup(handlerHelper.getMainMenuKeyboard(user.getLanguage().getLocale()))
                    .build();
            telegramBot.sendAnswerMessage(sendMessage);
            user.setBotState(BotState.MAIN_MENU);
            userService.updateUser(user);
            return;
        }
        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("myOrders", null, user.getLanguage().getLocale()))
                .replyMarkup(ReplyKeyboardRemove.builder().removeKeyboard(true).build())
                .build();
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.SHOW_ORDERS);
        userService.updateUser(user);
        myOrdersHandler.sendOrderListToUser(user);
    }


    private void actionContactUs(User user) {
        SendMessage sendMessage = new SendMessage(String.valueOf(
                user.getTelegramId()), messageSource.getMessage(
                "contactUs",
                new Object[]{DataLoader.phoneNumber},
                user.getLanguage().getLocale()
        ));
        telegramBot.sendAnswerMessage(sendMessage);
        handlerHelper.resetMainMenu(user);
    }

    public void shoppingCart(User user, boolean setKeyboard) {
        List<OrderProduct> orderProduct = basketService.userShoppingCart(user);
        if (orderProduct.isEmpty()) {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(user.getTelegramId())
                    .text(messageSource.getMessage("yourCartEmpty", null, user.getLanguage().getLocale()))
                    .replyMarkup(handlerHelper.getMainMenuKeyboard(user.getLanguage().getLocale()))
                    .build();
            telegramBot.sendAnswerMessage(sendMessage);
            user.setBotState(BotState.MAIN_MENU);
            userService.updateUser(user);
            return;
        }
        Long totalAmount = basketService.getTotalUserCartAmount(user);
        InlineKeyboardMarkup keyboard = handlerHelper.getUserCartKeyboard(user.getLanguage().getLocale(), orderProduct);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("userCart", new Object[]{totalAmount}, user.getLanguage().getLocale()))
                .replyMarkup(keyboard)
                .build();
        telegramBot.sendAnswerMessage(SendMessage.builder()
                .chatId(user.getTelegramId())
                .text(messageSource.getMessage("orderTogether", null, user.getLanguage().getLocale()))
                .replyMarkup(new ReplyKeyboardRemove(true)).build());
        telegramBot.sendAnswerMessage(sendMessage);
        user.setBotState(BotState.USER_CART);
        userService.updateUser(user);
    }
}
