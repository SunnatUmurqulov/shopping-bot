package com.company.shoppingbotv2.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.company.shoppingbotv2.handler.callbackquery.CallbackQueryHandler;
import com.company.shoppingbotv2.handler.message.MessageHandler;
import com.company.shoppingbotv2.utils.AppConstants;

//@RestController
@RequiredArgsConstructor
public class WebHookController {
    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;

    @PostMapping("/callback" + AppConstants.BASE_UPDATE_HANDLER_URI)
    public ResponseEntity<?> handleUpdate(@RequestBody Update update) {

        return ResponseEntity.ok().build();
    }
}
