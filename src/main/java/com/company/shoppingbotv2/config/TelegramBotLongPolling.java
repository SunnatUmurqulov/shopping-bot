package com.company.shoppingbotv2.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotOptions;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.util.WebhookUtils;
import com.company.shoppingbotv2.entity.User;
import com.company.shoppingbotv2.entity.enums.BotState;
import com.company.shoppingbotv2.exception.ApiExceptionHandler;
import com.company.shoppingbotv2.exception.ApiNotWorkingException;
import com.company.shoppingbotv2.exception.SomethingWentWrongException;
import com.company.shoppingbotv2.handler.HandlerFactory;
import com.company.shoppingbotv2.handler.callbackquery.CallbackQueryHandler;
import com.company.shoppingbotv2.handler.message.MessageHandler;
import com.company.shoppingbotv2.handler.message.StartCommandHandler;
import com.company.shoppingbotv2.service.UserService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j

public class TelegramBotLongPolling implements LongPollingBot {
    @Autowired
    private TelegramBot absSender;
    @Autowired
    private UserService userService;
    @Autowired
    private HandlerFactory handlerFactory;
    @Autowired
    @Lazy
    private StartCommandHandler startCommandHandler;

    @Value("${app.bot.username}")
    private String botUsername;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    @Value("${app.bot.token}")
    private String botToken;
    @Autowired
    private ApiExceptionHandler exceptionHandler;

    /**
     * Return username of this bot
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Return bot token to access Telegram API
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        executor.submit(() -> onUpdatesReceived(update));
    }

    public void onUpdatesReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().getChat().isUserChat()) {
                Message message = update.getMessage();
                User user = userService.getOrCreate(message.getFrom());
                BotState botState = user.getBotState();
                if (message.hasText()) {
                    if (message.getText().equals("/start")) {
                        startCommandHandler.handle(message, user);
                        return;
                    }
                }
                MessageHandler handler = handlerFactory.createMessageHandler(botState);
                handler.handle(message, user);
            } else if (update.hasCallbackQuery()) {
                CallbackQuery callbackQuery = update.getCallbackQuery();
                if (!callbackQuery.getMessage().isUserMessage()) {
                    return;
                }
                User user = userService.getOrCreate(callbackQuery.getFrom());
                BotState botState = user.getBotState();
                CallbackQueryHandler handler = handlerFactory.createCallbackQueryHandler(botState);
                handler.handle(callbackQuery, user);
            }
        } catch (SomethingWentWrongException ex) {
            exceptionHandler.handleMethodArgumentNotValidException(ex);
        } catch (ApiNotWorkingException ex) {
            exceptionHandler.handleApiNotWorkingException(ex);
        } catch (RuntimeException ex) {
            exceptionHandler.handleServiceUnavailableException(ex);
        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        for (Update update : updates) {
            executor.submit(() -> onUpdatesReceived(update));
        }
    }

    /**
     * Gets options for current bot
     *
     * @return BotOptions object with options information
     */
    @Override
    public BotOptions getOptions() {
        return new DefaultBotOptions();
    }

    /**
     * Clear current webhook (if present) calling setWebhook method with empty url.
     */
    @Override
    public void clearWebhook() throws TelegramApiRequestException {
        WebhookUtils.clearWebhook(absSender);
    }
}
