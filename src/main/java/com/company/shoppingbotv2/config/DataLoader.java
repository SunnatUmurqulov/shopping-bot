package com.company.shoppingbotv2.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.company.shoppingbotv2.entity.enums.Language;
import com.company.shoppingbotv2.service.ApiClient;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    public static Map<Language, String> starterComment = null;
    public static String phoneNumber = null;
    private final ApiClient apiClient;
    @Override
    public void run(String... args) {
        try {
            starterComment = new HashMap<>();
            for (Language language : Language.values()) {
                starterComment.put(language, apiClient.getComment(language));
            }
            phoneNumber = apiClient.getContact();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void yourTaskMethod() {
        this.run();
    }
}
