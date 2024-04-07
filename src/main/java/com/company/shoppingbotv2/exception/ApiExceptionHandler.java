package com.company.shoppingbotv2.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.company.shoppingbotv2.config.TelegramBot;
import com.company.shoppingbotv2.utils.AppConstants;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ApiExceptionHandler {
    private final TelegramBot telegramBot;

    @ExceptionHandler(value = {SomethingWentWrongException.class})
    public ResponseEntity<?> handleMethodArgumentNotValidException(
            SomethingWentWrongException ex) {
        SendMessage sendMessage = new SendMessage(AppConstants.ADMIN_TELEGRAM_ID, ex.getInfo());
        ex.printStackTrace();
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Xatolik yuz berdi xatolik (SomethingWentWrongException): {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(value = {ApiNotWorkingException.class})
    public ResponseEntity<?> handleApiNotWorkingException(
            ApiNotWorkingException ex) {
        SendMessage sendMessage = new SendMessage(AppConstants.ADMIN_TELEGRAM_ID, ex.getInfo());
        ex.printStackTrace();
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Xatolik yuz berdi xatolik (ApiNotWorkingException): {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<?> handleServiceUnavailableException(
            RuntimeException ex) {
        SendMessage sendMessage = new SendMessage(
                AppConstants.ADMIN_TELEGRAM_ID,
                String.format("Xatolik yuz berdi (RuntimeException)\n\n```\n%s\n```", ex.getMessage())
                );
        ex.printStackTrace();
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Xatolik yuz berdi xatolik (RuntimeException): {}", e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
