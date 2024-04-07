package com.company.shoppingbotv2.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.WebhookBot;
import com.company.shoppingbotv2.utils.AppConstants;

//@Component
@Slf4j
public class TelegramBotWebhook implements WebhookBot {
    private final String botUsername;
    private final String serverAddress;
    private final String secretKey;
    private final String botToken;

    public TelegramBotWebhook(@Value("${app.bot.token}") String botToken,
                              @Value("${app.bot.username}") String botUsername,
                              @Value("${app.server-address}") String serverAddress,
                              @Value("${app.secret-key}") String secretKey) {
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.serverAddress = serverAddress;
        this.secretKey = secretKey;
    }

    @PostConstruct
    private void init() {
        try {
            SetWebhook webhook = SetWebhook.builder()
                    .url(this.serverAddress)
//                    .ipAddress(this.serverAddress)
                    .secretToken(this.secretKey)
                    .build();
            this.setWebhook(webhook);
            log.info("The telegram bot was configured to: {} url", this.serverAddress);
        } catch (TelegramApiException e) {
            log.error("Something error while set webhook: {}", e.getMessage());
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public void setWebhook(SetWebhook setWebhook) throws TelegramApiException {

    }

    @Override
    public String getBotPath() {
        return AppConstants.BASE_UPDATE_HANDLER_URI;
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    /**
     * Return bot token to access Telegram API
     */
    @Override
    public String getBotToken() {
        return botToken;
    }
}
