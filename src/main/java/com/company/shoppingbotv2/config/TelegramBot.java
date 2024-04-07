package com.company.shoppingbotv2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.company.shoppingbotv2.exception.SomethingWentWrongException;

@Service
public class TelegramBot extends DefaultAbsSender {
    public TelegramBot(@Value("${app.bot.token}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }

    public Message sendAnswerMessage(SendMessage sendMessage) {
        try {
            return this.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new SomethingWentWrongException("Xabar jo'natishda xatolik berdi", e);
        }
    }

    public boolean sendDeleteMessage(DeleteMessage deleteMessage) {
        try {
            return this.execute(deleteMessage);
        } catch (TelegramApiException e) {
            return false;
//            throw new SomethingWentWrongException("Xabarni o'chirishda xatolik berdi", e);
        }
    }

    public boolean sendAnswerCallbackQuery(AnswerCallbackQuery answerCallbackQuery) {
        try {
            return this.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            throw new SomethingWentWrongException("Answer callback query yuborishda xatolik berdi", e);
        }
    }

    public boolean sendChatAction(SendChatAction sendChatAction) {
        try {
            return this.execute(sendChatAction);
        } catch (TelegramApiException e) {
            throw new SomethingWentWrongException("Chat action yuborishda xatolik berdi", e);
        }
    }

    public void sendDocument(SendDocument sendDocument) {
        try {
            this.execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new SomethingWentWrongException("Document yuborishda xatolik berdi", e);
        }
    }

    public void sendEditMessage(EditMessageText editMessage) {
        try {
            this.execute(editMessage);
        } catch (TelegramApiException e) {
            throw new SomethingWentWrongException("Xabarni o'zgartirishda xatolik berdi", e);
        }
    }
}
