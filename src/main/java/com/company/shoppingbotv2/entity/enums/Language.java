package com.company.shoppingbotv2.entity.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public enum Language {
    UZ("uz", "Uz", "\uD83C\uDDFA\uD83C\uDDFF O‘zbekcha", "uz"),
    RU("ru", "Ru", "\uD83C\uDDF7\uD83C\uDDFA Русский", "ru"),
    EN("en", "Us", "\uD83C\uDDEC\uD83C\uDDE7 English", "eng");

    private final String code;
    private final String countryCode;
    private final Locale locale;
    private final String text;
    private final String apiCode;

    Language(String code, String countryCode, String text, String apiCode) {
        this.code = code;
        this.countryCode = countryCode;
        this.locale = new Locale(code, countryCode);
        this.text = text;
        this.apiCode = apiCode;
    }

    public static List<String> getTexts() {
        List<String> result = new ArrayList<>();
        for (Language language : Language.values()) {
            result.add(language.text);
        }
        return result;
    }

    public static Language getLanguageByText(String text) {
        for (Language language : Language.values()) {
            if (language.text.equals(text)) return language;
        }
        return null;
    }
}
