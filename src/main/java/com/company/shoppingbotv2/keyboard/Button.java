package com.company.shoppingbotv2.keyboard;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.EnumSet;
import java.util.Locale;

public enum Button {
    GET_CONTRACT_DOC("getContractDocButton"),
    GO_BACK("backToButton"),
    BACK_MAIN_MENU("backToMainMenu");

    @Component
    public static class MessageSourceInjector {
        @Autowired
        private MessageSource messageSource;

        @PostConstruct
        public void postConstruct() {
            for (Button rt : EnumSet.allOf(Button.class))
                rt.setMessageSource(messageSource);
        }
    }

    @Setter
    private MessageSource messageSource;
    private final String sourceBundleKey;
    private boolean requestContact;
    private boolean requestLocation;

    Button() {
        this(null);
    }

    Button(String sourceBundleKey) {
        this.sourceBundleKey = sourceBundleKey;
    }

    Button(String sourceBundleKey, boolean requestContact, boolean requestLocation) {
        this.sourceBundleKey = sourceBundleKey;
        this.requestContact = requestContact;
        this.requestLocation = requestLocation;
    }

    public boolean pressed(Message message, Locale locale) {
        if (!message.hasText()) return false;
        return messageSource.getMessage(this.sourceBundleKey, null, locale).equals(message.getText());
    }

    public KeyboardButton getButton(Locale locale) {
        return KeyboardButton.builder()
                .text(messageSource.getMessage(this.sourceBundleKey, null, locale))
                .requestContact(this.requestContact ? true : null)
                .requestLocation(this.requestLocation ? true : null)
                .build();
    }
}
