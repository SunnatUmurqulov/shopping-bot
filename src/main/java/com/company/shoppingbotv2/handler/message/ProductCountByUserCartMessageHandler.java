package com.company.shoppingbotv2.handler.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.entity.OrderProduct;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.exception.SomethingWentWrongException;
import com.company.shoppingbotv2.repository.OrderProductRepository;
import com.company.shoppingbotv2.service.UserService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductCountByUserCartMessageHandler implements MessageHandler {
    private final TelegramBot telegramBot;
    private final MessageSource messageSource;
    private final OrderProductRepository orderProductRepository;
    private final UserService userService;
    private final MainMenuHandler mainMenuHandler;

    @Override
    public void handle(Message message, User user) {
        if (!message.hasText()) {
            resetHandler(message, user);
            return;
        }
        String productCountStr = message.getText();
        productCountStr = productCountStr.replace(',', '.');
        double productCount = 0;
        try {
            productCount = Double.parseDouble(productCountStr);
        } catch (NumberFormatException e) {
            resetHandler(message, user);
        }
        Integer orderProductId = user.getCurrentOrderProductForCounter();
        OrderProduct orderProduct = orderProductRepository.findById(orderProductId).orElseThrow(() -> new SomethingWentWrongException("order product idsi buyicha topilmadi", Map.of("id", orderProductId)));
        orderProduct.setCount(productCount);
        if (orderProduct.getRemainder() < orderProduct.getCount()) {
            telegramBot.sendAnswerMessage(SendMessage.builder()
                            .text(messageSource.getMessage("amountIsLarge", null, user.getLanguage().getLocale()))
                            .chatId(user.getTelegramId())
                            .replyToMessageId(message.getMessageId())
                            .allowSendingWithoutReply(true)
                    .build());
            return;
        }
        user.setBotState(BotState.PRODUCT_VIEW);
        userService.updateUser(user);
        orderProductRepository.save(orderProduct);
        mainMenuHandler.shoppingCart(user, false);
    }

    private void resetHandler(Message message, User user) {
        telegramBot.sendDeleteMessage(new DeleteMessage(String.valueOf(user.getTelegramId()), message.getMessageId() - 2));
        telegramBot.sendAnswerMessage(new SendMessage(String.valueOf(user.getTelegramId()), messageSource.getMessage("productCountError", null, user.getLanguage().getLocale())));
        user.setBotState(BotState.PRODUCT_COUNTER_USER_CART);
    }
}
